package il.ac.technion.cs.sd.buy.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class XMLParser extends Parser{
    public XMLParser(String xml) {
        Document document;
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
        orders.put(orderId, new Order(orderId, userId, productId, Integer.parseInt(amount)));
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
}
