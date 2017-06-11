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
    CompletableFuture<BuyProductReader> futureReader = setup("large.xml");

    //800 and not old price of 1000
    Long expected = (long)(7 * 500 + 4 * 800);
    assertEquals(expected, futureReader.thenCompose(reader -> reader.getTotalAmountSpentByUser("nerd")).get());
  }

  @Test
  public void shouldIgnoreOrdersOfNonExistentProducts() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("large.xml");

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
  public void shouldIgnoreOrdersOfNonExistentProducts2() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("large.xml");

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
}
