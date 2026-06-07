package util;

import models.Coordinates;
import models.Location;
import models.Route;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 * Утилиты для сериализации и десериализации коллекции маршрутов в XML.
 *
 * <p>Формат XML:</p>
 * <pre>
 * &lt;routes&gt;
 *   &lt;route&gt;
 *     &lt;id&gt;1&lt;/id&gt;
 *     &lt;name&gt;Домой&lt;/name&gt;
 *     &lt;coordinates&gt;&lt;x&gt;10.5&lt;/x&gt;&lt;y&gt;20&lt;/y&gt;&lt;/coordinates&gt;
 *     &lt;creationDate&gt;2024-01-01T10:00:00&lt;/creationDate&gt;
 *     &lt;from&gt;&lt;x&gt;1&lt;/x&gt;&lt;y&gt;2.0&lt;/y&gt;&lt;z&gt;3&lt;/z&gt;&lt;/from&gt;
 *     &lt;to&gt;&lt;x&gt;5&lt;/x&gt;&lt;y&gt;6.0&lt;/y&gt;&lt;z&gt;7&lt;/z&gt;&lt;/to&gt;
 *     &lt;distance&gt;100&lt;/distance&gt;
 *   &lt;/route&gt;
 * &lt;/routes&gt;
 * </pre>
 *
 * @author Hamid
 * @version 1.0
 */
public class XmlUtil {

    /** Приватный конструктор — только статические методы. */
    private XmlUtil() {}

    /**
     * Сериализует коллекцию маршрутов в XML-строку.
     *
     * @param collection коллекция маршрутов
     * @return XML-строка
     */
    public static String serialize(TreeSet<Route> collection) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<routes>\n");
        for (Route r : collection) {
            sb.append("  <route>\n");
            sb.append("    <id>").append(r.getId()).append("</id>\n");
            sb.append("    <name>").append(escape(r.getName())).append("</name>\n");
            sb.append("    <coordinates>\n");
            sb.append("      <x>").append(r.getCoordinates().getX()).append("</x>\n");
            sb.append("      <y>").append(r.getCoordinates().getY()).append("</y>\n");
            sb.append("    </coordinates>\n");
            sb.append("    <creationDate>").append(DateUtil.format(r.getCreationDate())).append("</creationDate>\n");
            sb.append("    <from>\n");
            sb.append(locationToXml(r.getFrom(), "      "));
            sb.append("    </from>\n");
            if (r.getTo() != null) {
                sb.append("    <to>\n");
                sb.append(locationToXml(r.getTo(), "      "));
                sb.append("    </to>\n");
            }
            if (r.getDistance() != null) {
                sb.append("    <distance>").append(r.getDistance()).append("</distance>\n");
            }
            sb.append("  </route>\n");
        }
        sb.append("</routes>");
        return sb.toString();
    }

    /**
     * Десериализует XML-строку в список маршрутов.
     *
     * @param xml XML-строка
     * @return список маршрутов (может быть пустым)
     * @throws Exception если XML невалиден или данные некорректны
     */
    public static List<Route> deserialize(String xml) throws Exception {
        List<Route> result = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        doc.getDocumentElement().normalize();

        NodeList nodes = doc.getElementsByTagName("route");
        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                Route route = parseRoute((Element) nodes.item(i));
                result.add(route);
            } catch (Exception e) {
                System.out.println("[ПРЕДУПРЕЖДЕНИЕ] Пропущен маршрут #" + (i + 1) + ": " + e.getMessage());
            }
        }
        return result;
    }



    /**
     * Разбирает один элемент &lt;route&gt; в объект {@link Route}.
     *
     * @param el XML-элемент route
     * @return объект Route
     * @throws Exception если обязательные поля отсутствуют или имеют неверный формат
     */
    private static Route parseRoute(Element el) throws Exception {
        int id = Integer.parseInt(text(el, "id"));
        String name = text(el, "name");

        Element coordEl = (Element) el.getElementsByTagName("coordinates").item(0);
        Float cx = Float.parseFloat(text(coordEl, "x"));
        Long cy = Long.parseLong(text(coordEl, "y"));
        Coordinates coords = new Coordinates(cx, cy);

        Date creationDate;
        try {
            creationDate = DateUtil.parse(text(el, "creationDate"));
        } catch (ParseException e) {
            creationDate = new Date();
        }

        Element fromEl = (Element) el.getElementsByTagName("from").item(0);
        Location from = parseLocation(fromEl);

        Location to = null;
        NodeList toNodes = el.getElementsByTagName("to");
        if (toNodes.getLength() > 0) {
            to = parseLocation((Element) toNodes.item(0));
        }

        Long distance = null;
        NodeList distNodes = el.getElementsByTagName("distance");
        if (distNodes.getLength() > 0) {
            String distText = distNodes.item(0).getTextContent().trim();
            if (!distText.isBlank()) distance = Long.parseLong(distText);
        }

        return new Route(id, name, coords, creationDate, from, to, distance);
    }

    /**
     * Разбирает элемент локации (&lt;from&gt; или &lt;to&gt;).
     *
     * @param el XML-элемент локации
     * @return объект {@link Location}
     */
    private static Location parseLocation(Element el) {
        long x = Long.parseLong(text(el, "x"));
        Double y = Double.parseDouble(text(el, "y"));
        Long z = Long.parseLong(text(el, "z"));
        return new Location(x, y, z);
    }

    /**
     * Возвращает текстовое содержимое первого дочернего тега с именем {@code tag}.
     *
     * @param parent родительский элемент
     * @param tag    имя тега
     * @return обрезанная строка содержимого
     */
    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent().trim();
    }

    /**
     * Сериализует локацию в XML-строку с заданным отступом.
     *
     * @param loc    локация
     * @param indent строка отступа
     * @return XML-строка полей локации
     */
    private static String locationToXml(Location loc, String indent) {
        return indent + "<x>" + loc.getX() + "</x>\n" +
                indent + "<y>" + loc.getY() + "</y>\n" +
                indent + "<z>" + loc.getZ() + "</z>\n";
    }

    /**
     * Экранирует специальные символы XML в строке.
     *
     * @param s исходная строка
     * @return экранированная строка
     */
    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}