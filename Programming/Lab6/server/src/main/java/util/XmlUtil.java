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

public class XmlUtil {

    private XmlUtil() {}

    public static String serialize(TreeSet<Route> collection) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<routes>\n");
        for (Route r : collection) {
            sb.append("  <route>\n");
            sb.append("    <id>").append(r.getId()).append("</id>\n");
            sb.append("    <name>").append(escape(r.getName())).append("</name>\n");
            sb.append("    <coordinates>\n");
            sb.append("      <x>").append(r.getCoordinates().getX()).append("</x>\n");
            sb.append("      <y>").append(r.getCoordinates().getY()).append("</y>\n");
            sb.append("    </coordinates>\n");
            sb.append("    <creationDate>").append(DateUtil.format(r.getCreationDate())).append("</creationDate>\n");
            sb.append("    <from>\n").append(locationXml(r.getFrom(), "      ")).append("    </from>\n");
            if (r.getTo() != null) {
                sb.append("    <to>\n").append(locationXml(r.getTo(), "      ")).append("    </to>\n");
            }
            if (r.getDistance() != null) {
                sb.append("    <distance>").append(r.getDistance()).append("</distance>\n");
            }
            sb.append("  </route>\n");
        }
        sb.append("</routes>");
        return sb.toString();
    }

    public static List<Route> deserialize(String xml) throws Exception {
        List<Route> result = new ArrayList<>();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        doc.getDocumentElement().normalize();
        NodeList nodes = doc.getElementsByTagName("route");
        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                result.add(parseRoute((Element) nodes.item(i)));
            } catch (Exception e) {
                System.out.println("[WARN] Skipped malformed route #" + (i + 1) + ": " + e.getMessage());
            }
        }
        return result;
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private static Route parseRoute(Element el) throws ParseException {
        int    id   = Integer.parseInt(text(el, "id"));
        String name = text(el, "name");

        Element ce = (Element) el.getElementsByTagName("coordinates").item(0);
        Float cx = Float.parseFloat(text(ce, "x"));
        Long  cy = Long.parseLong(text(ce, "y"));
        Coordinates coords = new Coordinates(cx, cy);

        Date creationDate;
        try { creationDate = DateUtil.parse(text(el, "creationDate")); }
        catch (ParseException e) { creationDate = new Date(); }

        Element fe = (Element) el.getElementsByTagName("from").item(0);
        Location from = parseLocation(fe);

        Location to = null;
        NodeList toNodes = el.getElementsByTagName("to");
        if (toNodes.getLength() > 0) to = parseLocation((Element) toNodes.item(0));

        Long distance = null;
        NodeList dn = el.getElementsByTagName("distance");
        if (dn.getLength() > 0 && !dn.item(0).getTextContent().isBlank())
            distance = Long.parseLong(dn.item(0).getTextContent().trim());

        return new Route(id, name, coords, creationDate, from, to, distance);
    }

    private static Location parseLocation(Element el) {
        long   x = Long.parseLong(text(el, "x"));
        Double y = Double.parseDouble(text(el, "y"));
        Long   z = Long.parseLong(text(el, "z"));
        return new Location(x, y, z);
    }

    private static String locationXml(Location loc, String indent) {
        return indent + "<x>" + loc.getX() + "</x>\n" +
                indent + "<y>" + loc.getY() + "</y>\n" +
                indent + "<z>" + loc.getZ() + "</z>\n";
    }

    private static String text(Element el, String tag) {
        return el.getElementsByTagName(tag).item(0).getTextContent().trim();
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}