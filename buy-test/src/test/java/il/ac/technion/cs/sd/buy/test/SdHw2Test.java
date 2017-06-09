package il.ac.technion.cs.sd.buy.test;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class SdHw2Test {

/*    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Rule
    public ExpectedException thrown = ExpectedException.none();*/

    public static String getFilesContent(String fileName) throws FileNotFoundException {
        return new Scanner(new File(XMLParserTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    }

}
