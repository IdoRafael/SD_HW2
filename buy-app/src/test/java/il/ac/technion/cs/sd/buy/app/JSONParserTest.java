package il.ac.technion.cs.sd.buy.app;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Yaniv on 10/06/2017.
 */
public class JSONParserTest {

    public static String getFilesContent(String fileName) throws FileNotFoundException {
        return new Scanner(new File(JSONParserTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    }

    public void justRun(String fileName)throws FileNotFoundException {
        String json = getFilesContent(fileName);
        JSONParser.parseJSONToSortedMap(json);
    }

    @Test
    public void smallTest() throws Exception{
        justRun("small.json");
    }
}