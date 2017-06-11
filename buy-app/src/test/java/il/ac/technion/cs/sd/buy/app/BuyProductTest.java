package il.ac.technion.cs.sd.buy.app;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuyProductTest {

  @Rule public Timeout globalTimeout = Timeout.seconds(30);

  private static CompletableFuture<BuyProductReader> setup(String fileName) throws FileNotFoundException {
    String fileContents =
        new Scanner(new File(BuyProductTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    Injector injector = Guice.createInjector(new BuyProductTestModule());
    BuyProductInitializer bpi = injector.getInstance(BuyProductInitializer.class);

    return (fileName.endsWith("xml") ? bpi.setupXml(fileContents) : bpi.setupJson(fileContents))
            .thenApply(v -> injector.getInstance(BuyProductReader.class));
  }

  @Test
  public void testSimpleXml() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("small.xml");
    assertEquals(Arrays.asList(5, 10, -1), futureReader.thenCompose(reader -> reader.getHistoryOfOrder("1")).get());
  }

  @Test
  public void testSimpleJson() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("small.json");

    assertEquals(
            2 * 10000 + 5 * 100 + 100 * 1,
            futureReader.thenCompose(
                    reader -> reader.getTotalAmountSpentByUser("1")
            ).get().intValue()
    );
  }

  @Test
  public void testSimpleJson2() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("small_2.json");

    CompletableFuture<Boolean> isValidOrderId = futureReader.thenCompose(reader -> reader.isValidOrderId("foo1234"));
    CompletableFuture<Boolean> isModifiedOrder = futureReader.thenCompose(reader -> reader.isModifiedOrder("foo1234"));
    CompletableFuture<Boolean> isCanceledOrder = futureReader.thenCompose(reader -> reader.isCanceledOrder("foo1234"));

    allOf(isValidOrderId, isModifiedOrder, isCanceledOrder).get();

    assertTrue(isValidOrderId.get());
    assertTrue(isModifiedOrder.get());
    assertTrue(isCanceledOrder.get());
  }
}
