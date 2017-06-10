package il.ac.technion.cs.sd.buy.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.buy.library.FutureStorage;
import il.ac.technion.cs.sd.buy.library.FutureStorageFactory;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;

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
        SortedMap<String, Order> orders = parser.getOrders();

        Comparator<String> csvStringComparator = Comparator
                .comparing((String s) -> s.split(DELIMITER)[0])
                .thenComparing((String s)-> s.split(DELIMITER)[1]);


        return allOf(
                setupUsersAndOrders(orders, csvStringComparator),
                setupOrdersAndProducts(orders, csvStringComparator),
                setupOrdersAndHistory(orders),
                setupProductsAndOrders(orders, csvStringComparator),
                setupUsersAndProducts(orders, csvStringComparator)
        );
    }

    private CompletableFuture<FutureStorage> setupUsersAndOrders(
            SortedMap<String, Order> orders,
            Comparator<String> comparator
    ) {
        SortedMap<String, String> usersAndOrders = new TreeMap<>(comparator);
        orders.forEach(
                (k, order) -> usersAndOrders.put(
                        String.join(DELIMITER, order.getUserId(), order.getOrderId()),
                        order.toString()
                )
        );
        return futureStorageFactory.create(
                usersAndOrdersFileName,
                String::compareTo,
                String::compareTo,
                usersAndOrders
        )
                .getFuture();

    }

    private CompletableFuture<FutureStorage> setupOrdersAndProducts(
            SortedMap<String, Order> orders,
            Comparator<String> comparator
    ) {
        SortedMap<String, String> ordersAndProducts = new TreeMap<>(comparator);
        orders.forEach(
                (k, order) -> ordersAndProducts.put(
                        String.join(DELIMITER, order.getOrderId(), order.getProductId()),
                        order.toString()
                )
        );
        return futureStorageFactory.create(
                ordersAndProductsFileName,
                String::compareTo,
                String::compareTo,
                ordersAndProducts
        )
                .getFuture();
    }

    private CompletableFuture<FutureStorage> setupOrdersAndHistory(
            SortedMap<String, Order> orders
    ) {
        Comparator<String> csvStringHistoryComparator = Comparator
                .comparing((String s) -> s.split(DELIMITER)[0])
                .thenComparing((String s)-> Integer.parseInt(s.split(DELIMITER)[1]));

        SortedMap<String, String> ordersAndHistory = new TreeMap<>(csvStringHistoryComparator);
        orders.forEach(
                (k, order) -> {
                    List<Integer> amountHistory = order.getAmountHistory();
                    IntStream
                            .range(0, amountHistory.size())
                            .forEach(
                                    i -> ordersAndHistory.put(
                                            String.join(
                                                    DELIMITER,order.getOrderId(),
                                                    ((Integer)i).toString()
                                            ),
                                            amountHistory.get(i).toString()
                                    )
                            );
                }
        );
        return futureStorageFactory.create(
                ordersAndHistoryFileName,
                String::compareTo,
                Comparator.comparing(Integer::parseInt),
                ordersAndHistory
        )
                .getFuture();
    }

    private CompletableFuture<FutureStorage> setupProductsAndOrders(
            SortedMap<String, Order> orders,
            Comparator<String> comparator
    ) {
        SortedMap<String, String> productsAndOrders = new TreeMap<>(comparator);
        orders.forEach(
                (k, order) -> productsAndOrders.put(
                        String.join(DELIMITER, order.getProductId(), order.getOrderId()),
                        order.toString()
                )
        );
        return futureStorageFactory.create(
                productsAndOrdersFileName,
                String::compareTo,
                String::compareTo,
                productsAndOrders
        )
                .getFuture();
    }

    private CompletableFuture<Void> setupUsersAndProducts(
            SortedMap<String, Order> orders,
            Comparator<String> comparator
    ) {
        Map<String, Order> groupedOrders = new HashMap<>();

        orders.forEach(
                (k, v) -> {
                    if (!v.isCancelled()) {
                        String key = String.join(",", v.getUserId(), v.getProductId());
                        Order oldOrder = groupedOrders.get(key);
                        if (oldOrder != null){
                            oldOrder.modifyAmount(oldOrder.getLatestAmount() + v.getLatestAmount());
                        } else {
                            groupedOrders.put(key, v);
                        }
                    }
                }
        );

        SortedMap<String, String> usersAndProducts = new TreeMap<>(comparator);
        SortedMap<String, String> productsAndUsers = new TreeMap<>(comparator);

        groupedOrders.forEach(
                (k, v) -> {
                    usersAndProducts.put(
                            String.join(",", v.getUserId(), v.getProductId()),
                            v.toString()
                    );
                    productsAndUsers.put(
                            String.join(",", v.getProductId(), v.getUserId()),
                            v.toString()
                    );
                }
        );

        return allOf(
                futureStorageFactory.create(
                        usersAndProductsFileName,
                        String::compareTo,
                        String::compareTo,
                        usersAndProducts
                )
                        .getFuture(),
                futureStorageFactory.create(
                        productsAndUsersFileName,
                        String::compareTo,
                        String::compareTo,
                        productsAndUsers
                )
                        .getFuture()
        );
    }
}
