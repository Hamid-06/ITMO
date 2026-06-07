package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private DateUtil() {}

    public static String format(Date date) {
        return new SimpleDateFormat(PATTERN).format(date);
    }

    public static Date parse(String s) throws ParseException {
        return new SimpleDateFormat(PATTERN).parse(s.trim());
    }
}