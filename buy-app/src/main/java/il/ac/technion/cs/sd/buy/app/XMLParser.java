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
import java.util.SortedMap;
import java.util.TreeMap;

public class XMLParser {

    private static final String DELIMITER = ",";

    private static SortedMap<String, String> products = new TreeMap<>();
    private static SortedMap<String, Order> orders = new TreeMap<>();


    public static SortedMap<String,String> parseXMLToSortedMap(String xml) {

        Comparator<String> csvStringComparator = Comparator
                .comparing((String s) -> s.split(DELIMITER)[0])
                .thenComparing((String s)-> s.split(DELIMITER)[1]);

        SortedMap<String, String> sortedByTwoKeys = new TreeMap<>(csvStringComparator);


        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            if (is == null){
                System.out.println("NULLLLLL");
            }
            System.out.println("NOT NULLLLLL");
            Document document = builder.parse(is);

            parseProducts(document);
            parseOrders(document);



/*            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr = xpath.compile(query);

            NodeList reviewList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < reviewList.getLength(); ++i) {
                Node reviewNode = reviewList.item(i);
                if (reviewNode.getNodeType() == Node.ELEMENT_NODE) {
                    String primaryId = reviewNode.getParentNode().getAttributes().getNamedItem("Id").getTextContent();
                    String secondaryId = reviewNode.getChildNodes().item(1).getTextContent();
                    String value = reviewNode.getChildNodes().item(3).getTextContent();

                    String keys;
                    if (swapKeys) {
                        keys = String.join(DELIMITER, secondaryId, primaryId);
                    } else {
                        keys = String.join(DELIMITER, primaryId, secondaryId);
                    }
                    sortedByTwoKeys.put(keys , value);
                }
            }*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return products;
    }

    private static void parseProducts(Document document) {
        NodeList nodeList = document.getElementsByTagName("Product");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String id = ((Element) node).getElementsByTagName("product-id").item(0).getTextContent();
                String price = ((Element) node).getElementsByTagName("price").item(0).getTextContent();
                System.out.println(id + " " + price);
                products.put(id, price);
            }
        }
    }

    private static void parseOrders(Document document){
        NodeList nodeList = document.getChildNodes().item(0).getChildNodes();
        for (int i=0 ; i < nodeList.getLength() ; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "Order":
                        handleOrder(node);
                    default:
                        break;
                }
            }
        }
    }

    private static void handleOrder(Node node){

    }


}
