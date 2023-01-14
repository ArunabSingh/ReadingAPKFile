package com.example.week2decodingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Document;

import fr.xgouchet.axml.CompressedXmlParser;
import fr.xgouchet.axml.CompressedXmlParserListener;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        unzipUpdateToCache();
        try {
            readCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            // Permission has already been granted, you can read from external storage
            System.out.println("Permission Granted Already");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                System.out.println("Permissions Granted after asking.");
            } else {
                // Permission denied
                System.out.println("Permissions Denied after asking.");
            }
        }
    }

    private void unzipUpdateToCache() {
        ZipInputStream zipIs = new ZipInputStream(getResources().openRawResource(R.raw.slack));
        ZipEntry ze = null;
        try {
            while ((ze = zipIs.getNextEntry()) != null) {
                if (ze.getName().equals("AndroidManifest.xml")) {
                    FileOutputStream fout = new FileOutputStream(getCacheDir().getAbsolutePath() + "/SlackAndroidManifest.xml");
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while ((length = zipIs.read(buffer))>0) {
                        fout.write(buffer, 0, length);
                    }
                    zipIs.closeEntry();
                    fout.close();
                }
            }
            zipIs .close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCache() throws IOException {
        FileInputStream is = new FileInputStream(new File(getCacheDir(), "SlackAndroidManifest.xml"));
        System.out.println("Print Manifest Content");
        try {
            Document doc = new CompressedXmlParser().parseDOM(is);
            dumpNode(doc.getChildNodes().item(0), "");
        }
        catch (Exception e) {
            System.err.println("Failed AXML decode: " + e);
            e.printStackTrace();
        } finally {
            is.close();
        }
    }

    private static void dumpNode(Node node, String indent) {
        System.out.println(indent + node.getNodeName() + " " + attrsToString(node.getAttributes()) + " -> " + node.getNodeValue());
        NodeList children = node.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; ++i)
            dumpNode(children.item(i), indent + "   ");
    }

    private static String attrsToString(NamedNodeMap attrs) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0, n = attrs.getLength(); i < n; ++i) {
            if (i != 0)
                sb.append(", ");
            Node attr = attrs.item(i);
            sb.append(attr.getNodeName()).append("=").append(attr.getNodeValue());
        }
        sb.append(']');
        return sb.toString();
    }

}