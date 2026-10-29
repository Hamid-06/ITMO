package io;

import java.io.File;
import java.io.IOException;


public class FileManager {


    public void checkReadable(String path) throws IOException {
        File f = new File(path);
        if (!f.exists())  throw new IOException("File not found: " + path);
        if (!f.isFile())  throw new IOException("Not a file: " + path);
        if (!f.canRead()) throw new IOException("No read permission: " + path);
    }


    public void checkWritable(String path) throws IOException {
        File f = new File(path);
        if (f.exists() && !f.canWrite())
            throw new IOException("No write permission: " + path);
        if (!f.exists()) {
            File parent = f.getParentFile();
            if (parent != null && !parent.canWrite())
                throw new IOException("Cannot write to directory: " + parent.getAbsolutePath());
        }
    }
}