package il.ac.technion.cs.sd.buy.test;

import il.ac.technion.cs.sd.buy.app.XMLParser;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Yaniv on 09/06/2017.
 */
public class XMLParserTest extends SdHw2Test {

    public void justRun(String fileName)throws FileNotFoundException {
        String xml = getFilesContent(fileName);
        XMLParser.parseXMLToSortedMap(xml);
    }

    @Test
    public void smallTest() throws Exception{
        justRun("small.xml");
    }
}
