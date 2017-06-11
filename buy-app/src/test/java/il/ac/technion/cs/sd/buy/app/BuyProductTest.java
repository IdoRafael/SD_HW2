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
import java.lang.reflect.Array;
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

    assertEquals(2.0, futureReader.thenCompose(
            reader -> reader.getAverageNumberOfItemsPurchased("snes")
    ).get().orElseThrow(RuntimeException::new), 0.00001);
  }

  @Test
  public void reorderingShouldReset3() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(0, futureReader.thenCompose(
            reader -> reader.getCancelRatioForUser("geek")
    ).get().orElseThrow(RuntimeException::new), 0.00001);

    assertEquals(0.5, futureReader.thenCompose(
            reader -> reader.getModifyRatioForUser("geek")
    ).get().orElseThrow(RuntimeException::new), 0.00001);

    CompletableFuture<Map<String, Long>> allItemsPurchased = futureReader.thenCompose(
            reader -> reader.getAllItemsPurchased("geek")
    );
    assertTrue(allItemsPurchased.get().containsKey("megadrive"));
    assertTrue(allItemsPurchased.get().containsKey("gameboy"));
    assertFalse(allItemsPurchased.get().containsKey("snes"));
    assertFalse(allItemsPurchased.get().containsKey("ps4"));

    CompletableFuture<Map<String, Long>> snesPurchasedByUsers = futureReader.thenCompose(
            reader -> reader.getItemsPurchasedByUsers("snes")
    );
    assertTrue(snesPurchasedByUsers.get().containsKey("dork"));
    assertFalse(snesPurchasedByUsers.get().containsKey("geek"));

    CompletableFuture<Map<String, Long>> ps4PurchasedByUsers = futureReader.thenCompose(
            reader -> reader.getItemsPurchasedByUsers("ps4")
    );
    assertTrue(ps4PurchasedByUsers.get().containsKey("noob"));

    assertEquals((Long)1L, ps4PurchasedByUsers.get().get("noob"));

    assertEquals(1, ps4PurchasedByUsers.get().size());
  }

  @Test
  public void modifyingOrderShouldCancelItsCancellation() throws Exception{
    CompletableFuture<BuyProductReader> futureReader = setup("large.xml");

    assertTrue(futureReader.thenCompose(
            reader1 -> reader1.getOrderIdsThatPurchased("turbografx")
    ).get().contains("10"));

    assertEquals(0.0,  futureReader.thenCompose(
            reader -> reader.getCancelRatioForUser("nerd")
    ).get().orElseThrow(RuntimeException::new), 0.00001);

    assertEquals(0.5,  futureReader.thenCompose(
            reader -> reader.getCancelRatioForUser("noob")
    ).get().orElseThrow(RuntimeException::new), 0.00001);

  }

  @Test
  public void shouldIgnoreModifyingOrderNonExistent() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertFalse(futureReader.thenCompose(
            reader -> reader.isValidOrderId("nonExistent")
    ).get());

    assertTrue(futureReader.thenCompose(
            reader -> reader.isValidOrderId("nonExistentYet")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isModifiedOrder("nonExistentYet")
    ).get());

    assertFalse(futureReader.thenCompose(
            reader -> reader.isCanceledOrder("nonExistentYet")
    ).get());

    assertEquals(1, futureReader.thenCompose(
            reader -> reader.getNumberOfProductOrdered("nonExistentYet")
    ).get().orElseThrow(RuntimeException::new));

  }

  @Test
  public void getNumberOfProductOrderedTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(7, futureReader.thenCompose(
            reader -> reader.getNumberOfProductOrdered("10")
    ).get().orElseThrow(RuntimeException::new));

    assertEquals(-11, futureReader.thenCompose(
            reader -> reader.getNumberOfProductOrdered("2")
    ).get().orElseThrow(RuntimeException::new));

    assertFalse(futureReader.thenCompose(
            reader -> reader.getNumberOfProductOrdered("nonExistent")
    ).get().isPresent());
  }

  @Test
  public void getHistoryOfOrderTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(
            Arrays.asList(1,2,3,4,5,6,7), futureReader.thenCompose(
                    reader -> reader.getHistoryOfOrder("10")).get()
    );

    assertEquals(
            Arrays.asList(11, -1), futureReader.thenCompose(
                    reader -> reader.getHistoryOfOrder("2")).get()
    );

    assertEquals(
            Arrays.asList(1, 3, -1), futureReader.thenCompose(
                    reader -> reader.getHistoryOfOrder("cancelled")).get()
    );

    assertEquals(
            new ArrayList<>(),
            futureReader.thenCompose(
                    reader -> reader
                            .getHistoryOfOrder("nonExistent")
            ).get()
    );
  }

  @Test
  public void getTotalAmountSpentByUserTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals((Long)0L, futureReader.thenCompose(
            reader -> reader.getTotalAmountSpentByUser("noSuchUser")).get()
    );

    //ignore cancelled
    assertEquals((Long) (100L + 4L * 400L), futureReader.thenCompose(
            reader -> reader.getTotalAmountSpentByUser("dork")).get()
    );
  }

  @Test
  public void getUsersThatPurchasedTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertTrue(futureReader.thenCompose(
            reader -> reader.getUsersThatPurchased("noSuchProduct")
            ).get().isEmpty()
    );

    assertTrue(futureReader.thenCompose(
            reader -> reader.isValidOrderId("abc")
            ).get()
    );

    assertTrue(futureReader.thenCompose(
            reader -> reader.isCanceledOrder("abc")
            ).get()
    );

    assertTrue(futureReader.thenCompose(
            reader -> reader.isModifiedOrder("abc")
            ).get()
    );

    CompletableFuture<List<String>> usersThatPurchasedPc = futureReader.thenCompose(
            reader -> reader.getUsersThatPurchased("pc")
    );

    assertTrue(usersThatPurchasedPc.get().contains("pcUser"));
    assertEquals(1, usersThatPurchasedPc.get().size());
    assertFalse(usersThatPurchasedPc.get().contains("cancelledUser"));
  }

  @Test
  public void getOrderIdsThatPurchasedTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(
            Arrays.asList("6", "notCancelled"),
            futureReader.thenCompose(
                    reader -> reader.getOrderIdsThatPurchased("snes")
            ).get()
    );

    //including cancelled
    assertTrue(
            futureReader.thenCompose(
                    reader -> reader.getOrderIdsThatPurchased("ps4")
            ).get().contains("2")
    );

  }

  @Test
  public void getTotalNumberOfItemsPurchasedTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("large.xml");

    assertEquals(OptionalLong.empty(), futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("cat")).get()
    );

    assertEquals(0, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("gamecom")).get().getAsLong()
    );

    assertEquals(30, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("megadrive")).get().getAsLong()
    );

    assertEquals(2, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("gameboy")).get().getAsLong()
    );

    assertEquals(1, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("ps4")).get().getAsLong()
    );
  }

  @Test
  public void getTotalNumberOfItemsPurchasedTest1() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("large.xml");

    assertEquals(4, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("vectrex")).get().getAsLong()
    );

    assertEquals(7, futureReader.thenCompose(
            reader -> reader.getTotalNumberOfItemsPurchased("turbografx")).get().getAsLong()
    );
  }

  @Test
  public void modifiedOrderedThatWhereCanceledShouldCountForRatio() throws Exception{
      CompletableFuture<BuyProductReader> futureReader = setup("large.xml");

      assertEquals(1.0, futureReader.thenCompose(
              reader -> reader.getModifyRatioForUser("noob")
      ).get().orElseThrow(RuntimeException::new), 0.00001);

      assertEquals(2.0/3.0, futureReader.thenCompose(
                      reader -> reader.getModifyRatioForUser("poke")
              ).get().orElseThrow(RuntimeException::new), 0.00001);
  }

  @Test
  public void getAverageNumberOfItemsPurchasedTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(OptionalDouble.empty(), futureReader.thenCompose(
            reader -> reader.getAverageNumberOfItemsPurchased("cat")).get()
    );

    assertEquals(2.0, futureReader.thenCompose(
            reader -> reader.getAverageNumberOfItemsPurchased("snes")).get()
            .orElseThrow(RuntimeException::new), 0.00001
    );

    assertEquals(2.0, futureReader.thenCompose(
            reader -> reader.getAverageNumberOfItemsPurchased("pc")).get()
            .orElseThrow(RuntimeException::new), 0.00001
    );

  }

  @Test
  public void getCancelRatioForUserTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(OptionalDouble.empty(), futureReader.thenCompose(
            reader -> reader.getCancelRatioForUser("cat")).get()
    );

    assertEquals(4.0 / 6.0, futureReader.thenCompose(
            reader -> reader.getCancelRatioForUser("pcUser")).get()
            .orElseThrow(RuntimeException::new), 0.00001
    );
  }

  @Test
  public void getModifyRatioForUserTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertEquals(OptionalDouble.empty(), futureReader.thenCompose(
            reader -> reader.getModifyRatioForUser("cat")).get()
    );

    assertEquals(5.0 / 6.0, futureReader.thenCompose(
            reader -> reader.getModifyRatioForUser("pcUser")).get()
            .orElseThrow(RuntimeException::new), 0.00001
    );
  }

  @Test
  public void getAllItemsPurchasedTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertTrue(futureReader.thenCompose(
            reader -> reader.getAllItemsPurchased("cat")).get().isEmpty()
    );

    assertEquals((Long)4L, futureReader.thenCompose(
            reader -> reader.getAllItemsPurchased("pcUser"))
            .get().get("pc")
    );
  }

  @Test
  public void getItemsPurchasedByUsersTest() throws Exception {
    CompletableFuture<BuyProductReader> futureReader = setup("applicationTest.json");

    assertTrue(futureReader.thenCompose(
            reader -> reader.getItemsPurchasedByUsers("cat")).get().isEmpty()
    );

    assertEquals((Long)4L, futureReader.thenCompose(
            reader -> reader.getItemsPurchasedByUsers("pc"))
            .get().get("pcUser")
    );
  }

}