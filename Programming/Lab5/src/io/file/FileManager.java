package io.file;

import exception.FileException;

import java.io.File;

/**
 * Проверяет доступность файлов для чтения и записи.
 *
 * @author Hamid
 * @version 1.0
 */
public class FileManager {

    /**
     * Проверяет что файл существует и доступен для чтения.
     *
     * @param path путь к файлу
     * @throws FileException если файл не найден или нет прав на чтение
     */
    public void checkReadable(String path) throws FileException {
        File f = new File(path);
        if (!f.exists()) throw new FileException("Файл не найден: " + path);
        if (!f.isFile()) throw new FileException("Путь не является файлом: " + path);
        if (!f.canRead()) throw new FileException("Нет прав на чтение файла: " + path);
    }

    /**
     * Проверяет что файл доступен для записи.
     * Если файл не существует — проверяет что родительская директория доступна.
     *
     * @param path путь к файлу
     * @throws FileException если нет прав на запись
     */
    public void checkWritable(String path) throws FileException {
        File f = new File(path);
        if (f.exists() && !f.canWrite()) {
            throw new FileException("Нет прав на запись в файл: " + path);
        }
        if (!f.exists()) {
            File parent = f.getParentFile();
            if (parent != null && !parent.canWrite()) {
                throw new FileException("Нет прав на запись в директорию: " + parent.getPath());
            }
        }
    }
}