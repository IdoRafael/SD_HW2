package il.ac.technion.cs.sd.buy.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.buy.library.StorageFactory;

import javax.inject.Named;
import java.util.concurrent.CompletableFuture;

public class BuyProductInitializerImpl implements BuyProductInitializer{
    private StorageFactory storageFactory;
    private String usersAndOrdersFileName;
    private String ordersAndProductsFileName;
    private String ordersAndHistoryFileName;
    private String productsAndOrdersFileName;
    private String usersAndProductsFileName;
    private String productsAndUsersFileName;

    @Inject
    public BuyProductInitializerImpl(
            StorageFactory storageFactory,
            @Named("usersAndOrdersFileName") String usersAndOrdersFileName,
            @Named("ordersAndProductsFileName") String ordersAndProductsFileName,
            @Named("ordersAndHistoryFileName") String ordersAndHistoryFileName,
            @Named("productsAndOrdersFileName") String productsAndOrdersFileName,
            @Named("usersAndProductsFileName") String usersAndProductsFileName,
            @Named("productsAndUsersFileName") String productsAndUsersFileName)
    {
        this.storageFactory = storageFactory;
        this.usersAndOrdersFileName = usersAndOrdersFileName;
        this.ordersAndProductsFileName = ordersAndProductsFileName;
        this.ordersAndHistoryFileName = ordersAndHistoryFileName;
        this.productsAndOrdersFileName = productsAndOrdersFileName;
        this.usersAndProductsFileName = usersAndProductsFileName;
        this.productsAndUsersFileName = productsAndUsersFileName;
    }

    @Override
    public CompletableFuture<Void> setupXml(String xmlData) {
        //SEE THIS FIRST!!!
        //https://piazza.com/class/j0f77eij4k2266?cid=87
        return null;
    }

    @Override
    public CompletableFuture<Void> setupJson(String jsonData) {
        return null;
    }

    private CompletableFuture<Void> setup(String data, Parser parser) {


        //TODO TMEP LOLZ
        return CompletableFuture.completedFuture(null);
    }
}
