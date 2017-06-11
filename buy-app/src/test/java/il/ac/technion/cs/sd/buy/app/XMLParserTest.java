package il.ac.technion.cs.sd.buy.app;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
        new XMLParser(xml).print();
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
}
