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
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExampleTest {

  @Rule public Timeout globalTimeout = Timeout.seconds(30);

  private static CompletableFuture<BuyProductReader> setup(String fileName) throws FileNotFoundException {
    String fileContents =
        new Scanner(new File(ExampleTest.class.getResource(fileName).getFile())).useDelimiter("\\Z").next();
    Injector injector = Guice.createInjector(new BuyProductModule(), new AbstractModule() {
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
}
