package il.ac.technion.cs.sd.buy.app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XMLParser extends Parser{

    private String xml;

    public XMLParser(String xml) {
        this.xml=xml;
        Document document = getDocumentFromString(xml);

        parseProducts(document);
        parseOrders(document);
    }

    private Document getDocumentFromString(String xml){
        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            document = builder.parse(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return document;
    }

    private void parseProducts(Document document) {
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

    private void parseOrders(Document document){
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

    private void handleOrder(Node node){
        String productId = ((Element) node).getElementsByTagName("product-id").item(0).getTextContent();
        if (!products.containsKey(productId)) {
            return;
        }
        String userId = ((Element) node).getElementsByTagName("user-id").item(0).getTextContent();
        String orderId = ((Element) node).getElementsByTagName("order-id").item(0).getTextContent();
        String amount = ((Element) node).getElementsByTagName("amount").item(0).getTextContent();
        orders.put(orderId, new Order(orderId, userId, productId, Integer.parseInt(amount), Integer.parseInt(products.get(productId))));
    }

    private void modifyOrder(Node node){
        String orderId = ((Element) node).getElementsByTagName("order-id").item(0).getTextContent();
        if (!orders.containsKey(orderId)){
            return;
        }
        String newAmount = ((Element) node).getElementsByTagName("new-amount").item(0).getTextContent();

        //no need to cancel, modifyAmount does this already
        //orders.get(orderId).setCancelled(false);
        orders.get(orderId).modifyAmount(Integer.parseInt(newAmount));
    }

    private void cancelOrder(Node node){
        String orderId = ((Element) node).getElementsByTagName("order-id").item(0).getTextContent();
        if (!orders.containsKey(orderId)) {
            return;
        }
        orders.get(orderId).setCancelled(true);
    }

    public String toJSON(){
        Document document = getDocumentFromString(xml);
        NodeList nodeList = document.getChildNodes().item(0).getChildNodes();
        JsonArray jsonArray = new JsonArray();
        for (int i=0 ; i < nodeList.getLength() ; ++i) {
            Node node = nodeList.item(i);
            JsonObject jsonObject = new JsonObject();
            if (node.getNodeType() == Node.ELEMENT_NODE && !node.getNodeName().equals("Root")) {
                switch (node.getNodeName()) {
                    case "Product":
                        jsonObject.addProperty("type", "product");
                        jsonObject.addProperty("id", ((Element) node).getElementsByTagName("id").item(0).getTextContent());
                        jsonObject.addProperty("price", ((Element) node).getElementsByTagName("price").item(0).getTextContent());
                        break;
                    case "Order":
                        jsonObject.addProperty("type", "order");
                        jsonObject.addProperty("order-id", ((Element) node).getElementsByTagName("order-id").item(0).getTextContent());
                        jsonObject.addProperty("user-id", ((Element) node).getElementsByTagName("user-id").item(0).getTextContent());
                        jsonObject.addProperty("product-id", ((Element) node).getElementsByTagName("product-id").item(0).getTextContent());
                        jsonObject.addProperty("amount", ((Element) node).getElementsByTagName("amount").item(0).getTextContent());
                        break;
                    case "ModifyOrder":
                        jsonObject.addProperty("type", "modify-order");
                        jsonObject.addProperty("order-id", ((Element) node).getElementsByTagName("order-id").item(0).getTextContent());
                        jsonObject.addProperty("amount", ((Element) node).getElementsByTagName("new-amount").item(0).getTextContent());
                        break;
                    case "CancelOrder":
                        jsonObject.addProperty("type", "cancel-order");
                        jsonObject.addProperty("order-id", ((Element) node).getElementsByTagName("order-id").item(0).getTextContent());
                        break;
                    default:
                        break;
                }
                jsonArray.add(jsonObject);
            }

        }

        return new Gson().toJson(jsonArray);
    }
}
