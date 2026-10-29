package io;

import models.Route;
import util.XmlUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

public class XmlWriter {

    private final FileManager fileManager = new FileManager();

    public void save(String filename, TreeSet<Route> collection) throws IOException {
        fileManager.checkWritable(filename);
        String xml = XmlUtil.serialize(collection);

        // Write using BufferedOutputStream (required by lab)
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filename))) {
            bos.write(xml.getBytes(StandardCharsets.UTF_8));
        }
    }
}