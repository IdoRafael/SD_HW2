package il.ac.technion.cs.sd.buy.test;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import il.ac.technion.cs.sd.buy.app.BuyProductInitializer;
import il.ac.technion.cs.sd.buy.app.BuyProductInitializerImpl;
import il.ac.technion.cs.sd.buy.app.BuyProductReader;
import il.ac.technion.cs.sd.buy.app.BuyProductReaderImpl;
import il.ac.technion.cs.sd.buy.library.Storage;
import il.ac.technion.cs.sd.buy.library.StorageFactory;
import il.ac.technion.cs.sd.buy.library.StringStorage;
//import il.ac.technion.cs.sd.buy.library.StringStorage;

// This module is in the testing project, so that it could easily bind all dependencies from all levels.
public class BuyProductModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(BuyProductInitializer.class).to(BuyProductInitializerImpl.class);
    bind(BuyProductReader.class).to(BuyProductReaderImpl.class);
    install(new FactoryModuleBuilder()
            .implement(Storage.class, StringStorage.class)
            .build(StorageFactory.class));

    bind(String.class)
            .annotatedWith(Names.named("reviewersFileName"))
            .toInstance("0");

    bind(String.class)
            .annotatedWith(Names.named("booksFileName"))
            .toInstance("1");

  }
}
