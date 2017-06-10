package il.ac.technion.cs.sd.buy.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.buy.library.FutureStorageFactory;

import javax.inject.Named;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.allOf;

public class BuyProductInitializerImpl implements BuyProductInitializer{
    private static final String DELIMITER = ",";

    private FutureStorageFactory futureStorageFactory;
    private String usersAndOrdersFileName;
    private String ordersAndProductsFileName;
    private String ordersAndHistoryFileName;
    private String productsAndOrdersFileName;
    private String usersAndProductsFileName;
    private String productsAndUsersFileName;

    @Inject
    public BuyProductInitializerImpl(
            FutureStorageFactory futureStorageFactory,
            @Named("usersAndOrdersFileName") String usersAndOrdersFileName,
            @Named("ordersAndProductsFileName") String ordersAndProductsFileName,
            @Named("ordersAndHistoryFileName") String ordersAndHistoryFileName,
            @Named("productsAndOrdersFileName") String productsAndOrdersFileName,
            @Named("usersAndProductsFileName") String usersAndProductsFileName,
            @Named("productsAndUsersFileName") String productsAndUsersFileName)
    {
        this.futureStorageFactory = futureStorageFactory;
        this.usersAndOrdersFileName = usersAndOrdersFileName;
        this.ordersAndProductsFileName = ordersAndProductsFileName;
        this.ordersAndHistoryFileName = ordersAndHistoryFileName;
        this.productsAndOrdersFileName = productsAndOrdersFileName;
        this.usersAndProductsFileName = usersAndProductsFileName;
        this.productsAndUsersFileName = productsAndUsersFileName;
    }

    @Override
    public CompletableFuture<Void> setupXml(String xmlData) {
        return setup(new XMLParser(xmlData));
    }

    @Override
    public CompletableFuture<Void> setupJson(String jsonData) {
        return setup(new JSONParser(jsonData));
    }

    private CompletableFuture<Void> setup(Parser parser) {
        SortedMap<String, String> products = parser.getProducts();
        SortedMap<String, Order> orders = parser.getOrders();

        Comparator<String> csvStringComparator = Comparator
                .comparing((String s) -> s.split(DELIMITER)[0])
                .thenComparing((String s)-> s.split(DELIMITER)[1]);

        //usersAndOrdersFileName
        SortedMap<String, String> usersAndOrders = new TreeMap<>(csvStringComparator);
        orders.forEach(
                (k, order) -> usersAndOrders.put(
                        String.join(DELIMITER, order.getUserId(), order.getOrderId()),
                        String.join(DELIMITER,
                                order.getLatestAmount().toString(),
                                serializeBoolean(order.isCancelled()),
                                serializeBoolean(order.isModified())
                        )
                )
        );
        CompletableFuture<Void> task0 = futureStorageFactory.create(usersAndOrdersFileName, usersAndOrders).getFuture();

        //ordersAndProductsFileName
        /*SortedMap<String, String> ordersAndProducts = new TreeMap<>(csvStringComparator);
        orders.forEach(
                (k, order) -> ordersAndProducts.put(
                        String.join(DELIMITER, order.getOrderId(), order.getProductId()),
                        String.join(DELIMITER,
                                order.getLatestAmount().toString(),
                                serializeBoolean(order.isCancelled()),
                                serializeBoolean(order.isModified())
                        )
                )
        );*/
        //CompletableFuture<Void> task1 = futureStorageFactory.create(filename, map).getFuture();

        //ordersAndHistoryFileName
        //CompletableFuture<Void> task2 = futureStorageFactory.create(filename, map).getFuture();

        //productsAndOrdersFileName
        //CompletableFuture<Void> task3 = futureStorageFactory.create(filename, map).getFuture();

        //usersAndProductsFileName
        //CompletableFuture<Void> task4 = futureStorageFactory.create(filename, map).getFuture();

        //productsAndUsersFileName
        //CompletableFuture<Void> task5 = futureStorageFactory.create(filename, map).getFuture();

//        return allOf(task0, task1, task2, task3, task4, task5);

        //TODO TEMP
        return null;
    }

    private String serializeBoolean(Boolean b) {
        return b ? "1" : "0";
    }
}
