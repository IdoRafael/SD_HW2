package il.ac.technion.cs.sd.buy.app;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Yaniv on 10/06/2017.
 */
public class JSONParserTest {

    public static String getFilesContent(String fileName) throws FileNotFoundException {
        return new Scanner(new File(JSONParserTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    }

    public void justRun(String fileName)throws FileNotFoundException {
        String json = getFilesContent(fileName);
        new JSONParser(json).print();
    }

    @Test
    public void smallTest() throws Exception{
        justRun("small.json");
    }

    @Test
    public void smallTest2() throws Exception{
        justRun("small_2.json");
    }

    @Test
    public void testProducts() throws Exception{
        SortedMap<String, String> products = new JSONParser(new XMLParser(getFilesContent("large.xml")).toJSON()).getProducts();
        assertEquals(products.size(),6);
        assertEquals(products.get("megadrive"),"200");
        assertEquals(products.get("vectrex"),"800");

    }

    @Test
    public void testOrders() throws Exception{
        SortedMap<String, Order> orders = new JSONParser(new XMLParser(getFilesContent("large.xml")).toJSON()).getOrders();
        assertEquals(orders.size(),9);
        assertEquals((long)orders.get("10").getLatestAmount(),7);
        assertEquals(orders.get("2").isCancelled(),true);
        assertEquals(orders.get("1").isCancelled(),false);
        assertEquals(orders.get("8").getUserId(),"nerd");
        assertEquals(orders.get("7").getProductId(),"gameboy");
        assertEquals(orders.get("10").getAmountHistory().size(),7);
        assertFalse(orders.containsKey("thisOrderWillBeCanceled"));
    }
}