package il.ac.technion.cs.sd.buy.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class XMLParser {

    private static final String DELIMITER = ",";

    private static SortedMap<String, String> products = new TreeMap<>();
    private static SortedMap<String, Order> orders = new TreeMap<>();


    public static void parseXMLToSortedMap(String xml) {

        Document document = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            document = builder.parse(is);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        parseProducts(document);
        parseOrders(document);

        System.out.println("Products");
        for (Map.Entry<String,String> entry : products.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println("Orders");
        for (Map.Entry<String,Order> entry : orders.entrySet()){
            System.out.println("orderId " + entry.getKey() + " userId " + entry.getValue().getUserId() + " productID "
                    + entry.getValue().getProductId() + " latestAmount " + entry.getValue().getLatestAmount());
            System.out.print("amount history: ");
            for(Integer amount : entry.getValue().getAmountHistory()){
                System.out.print(amount + " ");
            }
            System.out.println("");
        }
    }

    private static void parseProducts(Document document) {
        NodeList nodeList = document.getElementsByTagName("Product");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String id = ((Element) node).getElementsByTagName("id").item(0).getTextContent();
                String price = ((Element) node).getElementsByTagName("price").item(0).getTextContent();
                products.put(id, price);
            }
        }
    }

    private static void parseOrders(Document document){
        NodeList nodeList = document.getChildNodes().item(0).getChildNodes();
        for (int i=0 ; i < nodeList.getLength() ; ++i) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "Order":
                        handleOrder(node);
                        break;
                    case "ModifyOrder":
                        modifyOrder(node);
                        break;
                    case "CancelOrder":
                        cancelOrder(node);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static void handleOrder(Node node){
        String productId = ((Element) node).getElementsByTagName("product-id").item(0).getTextContent();
        if (!products.containsKey(productId)) {
            return;
        }
        String userId = ((Element) node).getElementsByTagName("user-id").item(0).getTextContent();
        String orderId = ((Element) node).getElementsByTagName("order-id").item(0).getTextContent();
        String amount = ((Element) node).getElementsByTagName("amount").item(0).getTextContent();
        orders.put(orderId, new Order(orderId, userId, productId, Integer.parseInt(amount)));
    }

    private static void modifyOrder(Node node){
        String orderId = ((Element) node).getElementsByTagName("order-id").item(0).getTextContent();
        if (!orders.containsKey(orderId)){
            return;
        }
        String newAmount = ((Element) node).getElementsByTagName("new-amount").item(0).getTextContent();
        orders.get(orderId).setCancelled(false);
        orders.get(orderId).modifyAmount(Integer.parseInt(newAmount));
    }

    private static void cancelOrder(Node node){
        String orderId = ((Element) node).getElementsByTagName("order-id").item(0).getTextContent();
        if (!orders.containsKey(orderId)) {
            return;
        }
        orders.get(orderId).setCancelled(true);
    }
}
