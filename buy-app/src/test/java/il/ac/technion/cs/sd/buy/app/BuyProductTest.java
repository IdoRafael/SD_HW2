package il.ac.technion.cs.sd.buy.app;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuyProductTest {

  @Rule public Timeout globalTimeout = Timeout.seconds(30);

  private static CompletableFuture<BuyProductReader> setup(String fileName) throws FileNotFoundException {
    String fileContents =
        new Scanner(new File(BuyProductTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    Injector injector = Guice.createInjector(new BuyProductTestModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(FutureLineStorageFactory.class).toInstance(new FutureLineStorageFactoryTestImpl());
      }
    });
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

    assertTrue(futureReader.thenCompose(reader -> reader.isValidOrderId("foo1234")).get());
    assertTrue(futureReader.thenCompose(reader -> reader.isModifiedOrder("foo1234")).get());
    assertTrue(futureReader.thenCompose(reader -> reader.isCanceledOrder("foo1234")).get());
  }

  @Test
  public void shouldGetLastProductDefinition() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    //800 and not old price of 1000
    Long expected = (long)(7 * 500 + 4 * 800);
    assertEquals(expected, futureReader.thenCompose(reader -> reader.getTotalAmountSpentByUser("nerd")).get());
  }

  @Test
  public void shouldIgnoreNonExistentProducts() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertTrue(futureReader.thenCompose(
            reader -> reader.getUsersThatPurchased("ps5")
    ).get().isEmpty());

    assertTrue(futureReader.thenCompose(
            reader -> reader.getOrderIdsThatPurchased("ps5")
    ).get().isEmpty());

    assertFalse(futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("ps5")
    ).get().isPresent());

    assertFalse(futureReader.thenCompose(
            reader -> reader.getAverageNumberOfItemsPurchased("ps5")
    ).get().isPresent());

    assertTrue(futureReader.thenCompose(
            reader -> reader.getItemsPurchasedByUsers("ps5")
    ).get().isEmpty());
  }

  @Test
  public void shouldIgnoreNonExistentProducts2() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertFalse(futureReader.thenCompose(
            reader -> reader.isValidOrderId("ps5")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isCanceledOrder("ps5")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isModifiedOrder("ps5")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.getNumberOfProductOrdered("ps5")
    ).get().isPresent());

    assertTrue(futureReader.thenCompose(
            reader -> reader.getHistoryOfOrder("ps5")
    ).get().isEmpty());
  }

  @Test
  public void shouldIgnoreNonExistentProducts3() throws Exception {
        CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

        assertFalse(futureReader.thenCompose(
                reader -> reader.getOrderIdsForUser("noob")
        ).get().contains("ps5"));

        assertFalse(futureReader.thenCompose(
                reader -> reader.getAllItemsPurchased("noob")
        ).get().containsKey("ps5"));
    }

  @Test
  public void reorderingShouldReset() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertTrue(futureReader.thenCompose(
            reader -> reader.isValidOrderId("6")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isCanceledOrder("6")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isModifiedOrder("6")
    ).get());

    assertEquals(2, futureReader.thenCompose(
            reader -> reader.getNumberOfProductOrdered("6")
    ).get().getAsInt());

    CompletableFuture<List<Integer>> historyOfOrder = futureReader.thenCompose(
            reader -> reader.getHistoryOfOrder("6")
    );

    assertEquals(1, historyOfOrder.get().size());
    assertTrue(historyOfOrder.get().contains(2));
  }

  @Test
  public void reorderingShouldReset1() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertTrue(futureReader.thenCompose(
            reader -> reader.isValidOrderId("notCancelled")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isCanceledOrder("notCancelled")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isModifiedOrder("notCancelled")
    ).get());

    assertEquals(2, futureReader.thenCompose(
            reader -> reader.getNumberOfProductOrdered("notCancelled")
    ).get().getAsInt());

    CompletableFuture<List<Integer>> historyOfOrder = futureReader.thenCompose(
            reader -> reader.getHistoryOfOrder("notCancelled")
    );

    assertEquals(1, historyOfOrder.get().size());
    assertTrue(historyOfOrder.get().contains(2));
  }

  @Test
  public void reorderingShouldReset2() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    CompletableFuture<List<String>> usersThatPurchasedSnes = futureReader.thenCompose(
            reader -> reader.getUsersThatPurchased("snes")
    );
    assertTrue(usersThatPurchasedSnes.get().contains("dork"));
    assertFalse(usersThatPurchasedSnes.get().contains("geek"));

    CompletableFuture<List<String>> orderIdsThatPurchased = futureReader.thenCompose(
            reader -> reader.getOrderIdsThatPurchased("ps4")
    );

    assertFalse(orderIdsThatPurchased.get().contains("6"));
    assertFalse(orderIdsThatPurchased.get().contains("notCancelled"));

    assertTrue(orderIdsThatPurchased.get().contains("1"));
    assertTrue(orderIdsThatPurchased.get().contains("2"));

    assertEquals(4, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("snes")
    ).get().getAsLong());

    assertEquals(1, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("ps4")
    ).get().getAsLong());

    assertEquals(0, Double.compare(2, futureReader.thenCompose(
            reader -> reader.getAverageNumberOfItemsPurchased("snes")
    ).get().orElseThrow(RuntimeException::new)));
  }

  @Test
  public void reorderingShouldReset3() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(0, Double.compare(0, futureReader.thenCompose(
            reader -> reader.getCancelRatioForUser("geek")
    ).get().orElseThrow(RuntimeException::new)));

    assertEquals(0, Double.compare(0.5, futureReader.thenCompose(
            reader -> reader.getModifyRatioForUser("geek")
    ).get().orElseThrow(RuntimeException::new)));
  }
}
