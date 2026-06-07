package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Утилиты для работы с датами.
 *
 * <p>Обеспечивает единый формат даты при записи в XML и чтении из него.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class DateUtil {

    /** Формат даты для XML. */
    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /** Приватный конструктор — только статические методы. */
    private DateUtil() {}

    /**
     * Форматирует дату в строку для записи в XML.
     *
     * @param date дата
     * @return строковое представление даты
     */
    public static String format(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(FORMAT).format(date);
    }

    /**
     * Разбирает строку даты из XML.
     *
     * @param s строка с датой
     * @return объект {@link Date}
     * @throws ParseException если формат строки неверный
     */
    public static Date parse(String s) throws ParseException {
        if (s == null || s.isBlank()) throw new ParseException("Empty date string", 0);
        return new SimpleDateFormat(FORMAT).parse(s.trim());
    }
}