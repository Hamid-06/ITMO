package io;

import managers.CollectionManager;
import models.Route;
import util.XmlUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class XmlReader {

    private final FileManager fileManager = new FileManager();

    public void load(String filename, CollectionManager manager) throws IOException {
        fileManager.checkReadable(filename);

        // Read entire file content using BufferedReader (required by lab)
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        // Parse XML and load routes, preserving original IDs
        try {
            List<Route> routes = XmlUtil.deserialize(sb.toString());
            routes.forEach(manager::loadFromFile);
            System.out.println("[INFO] Loaded " + routes.size() + " routes from " + filename);
        } catch (Exception e) {
            throw new IOException("Failed to parse XML: " + e.getMessage(), e);
        }
    }
}