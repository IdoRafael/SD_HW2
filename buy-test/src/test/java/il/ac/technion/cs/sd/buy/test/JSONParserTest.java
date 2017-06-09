package il.ac.technion.cs.sd.buy.test;

import il.ac.technion.cs.sd.buy.app.JSONParser;
import il.ac.technion.cs.sd.buy.app.XMLParser;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * Created by Yaniv on 10/06/2017.
 */
public class JSONParserTest extends SdHw2Test {

    public void justRun(String fileName)throws FileNotFoundException {
        String json = getFilesContent(fileName);
        JSONParser.parseJSONToSortedMap(json);
    }

    @Test
    public void smallTest() throws Exception{
        justRun("small.json");
    }
}