package il.ac.technion.cs.sd.buy.app;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Yaniv on 09/06/2017.
 */
public class XMLParserTest {

    public static String getFilesContent(String fileName) throws FileNotFoundException {
        return new Scanner(new File(XMLParserTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    }

    public void justRun(String fileName)throws FileNotFoundException {
        String xml = getFilesContent(fileName);
        new XMLParser(xml);
    }

    @Test
    public void smallTest() throws Exception{
        justRun("small.xml");
    }

    @Test
    public void largeTest() throws Exception{
        justRun("large.xml");
    }

    @Test
    public void convertToJSON() throws Exception{
        String xml = getFilesContent("large.xml");
        String json = new XMLParser(xml).toJSON();
        assertTrue(new XMLParser(xml).getProducts().equals(new JSONParser(json).getProducts()));
        assertTrue(new XMLParser(xml).getOrders().equals(new JSONParser(json).getOrders()));
    }

    @Test
    public void testProducts() throws Exception{
        SortedMap<String, String> products = new XMLParser(getFilesContent("large.xml")).getProducts();
        assertEquals(8, products.size());
        assertEquals(products.get("megadrive"),"200");
        assertEquals(products.get("vectrex"),"800");

    }

    @Test
    public void testOrders() throws Exception{
        SortedMap<String, Order> orders = new XMLParser(getFilesContent("large.xml")).getOrders();
        assertEquals(12, orders.size());
        assertEquals((long)orders.get("10").getLatestAmount(),7);
        assertEquals(orders.get("2").isCancelled(),true);
        assertEquals(orders.get("1").isCancelled(),false);
        assertEquals(orders.get("8").getUserId(),"nerd");
        assertEquals(orders.get("7").getProductId(),"gameboy");
        assertEquals(orders.get("10").getAmountHistory().size(),7);
        assertFalse(orders.containsKey("thisOrderWillBeCanceled"));
    }
}
