package io.file;

import exception.FileException;
import managers.CollectionManager;
import models.Route;
import util.XmlUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Читает коллекцию маршрутов из XML-файла.
 *
 * <p>Использует {@link BufferedReader} для построчного чтения файла
 * и {@link XmlUtil} для разбора XML.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class XmlReader {

    private final FileManager fileManager = new FileManager();

    /**
     * Загружает маршруты из файла в коллекцию.
     *
     * <p>При ошибке в отдельном маршруте — пропускает его и выводит предупреждение.
     * При полной невозможности прочитать файл — бросает исключение.</p>
     *
     * @param filename  путь к XML-файлу
     * @param manager   менеджер коллекции для загрузки данных
     * @throws FileException если файл недоступен
     * @throws IOException   если произошла ошибка чтения
     */
    public void load(String filename, CollectionManager manager)
            throws FileException, IOException {
        fileManager.checkReadable(filename);

        // читаем файл построчно через BufferedReader (требование задания)
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        // парсим XML и загружаем маршруты
        try {
            List<Route> routes = XmlUtil.deserialize(sb.toString());
            for (Route r : routes) {
                manager.loadFileFrom(r);  // сохраняет оригинальный id
            }
            System.out.println("Загружено маршрутов: " + routes.size());
        } catch (Exception e) {
            throw new FileException("Ошибка разбора XML: " + e.getMessage(), e );
        }
    }
}