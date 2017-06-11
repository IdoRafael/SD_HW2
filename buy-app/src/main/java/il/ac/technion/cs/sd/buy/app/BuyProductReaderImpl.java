package il.ac.technion.cs.sd.buy.app;

import com.google.inject.Inject;
import il.ac.technion.cs.sd.buy.ext.FutureLineStorage;
import il.ac.technion.cs.sd.buy.library.FutureStorage;
import il.ac.technion.cs.sd.buy.library.FutureStorageFactory;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.allOf;

public class BuyProductReaderImpl implements BuyProductReader {
    private CompletableFuture<Void> finishedConstructing;
    private static final String DELIMITER = ",";

    private CompletableFuture<FutureStorage> productsAndPrices;
    private CompletableFuture<FutureStorage> usersAndOrders;
    private CompletableFuture<FutureStorage> ordersAndProducts;
    private CompletableFuture<FutureStorage> ordersAndHistory;
    private CompletableFuture<FutureStorage> productsAndOrders;
    private CompletableFuture<FutureStorage> usersAndProducts;
    private CompletableFuture<FutureStorage> productsAndUsers;

    @Inject
    public BuyProductReaderImpl(
            FutureStorageFactory futureStorageFactory,
            @Named("productsAndPricesFileName") String productsAndPricesFileName,
            @Named("usersAndOrdersFileName") String usersAndOrdersFileName,
            @Named("ordersAndProductsFileName") String ordersAndProductsFileName,
            @Named("ordersAndHistoryFileName") String ordersAndHistoryFileName,
            @Named("productsAndOrdersFileName") String productsAndOrdersFileName,
            @Named("usersAndProductsFileName") String usersAndProductsFileName,
            @Named("productsAndUsersFileName") String productsAndUsersFileName
    )
    {
        this.productsAndPrices = futureStorageFactory.create(
                productsAndPricesFileName,
                String::compareTo,
                String::compareTo
        ).getFuture();

        this.usersAndOrders = futureStorageFactory.create(
                usersAndOrdersFileName,
                String::compareTo,
                String::compareTo
        ).getFuture();

        this.ordersAndProducts = futureStorageFactory.create(
                ordersAndProductsFileName,
                String::compareTo,
                String::compareTo
        ).getFuture();

        this.ordersAndHistory = futureStorageFactory.create(
                ordersAndHistoryFileName,
                String::compareTo,
                Comparator.comparing(Integer::parseInt)
        ).getFuture();


        this.productsAndOrders = futureStorageFactory.create(
                productsAndOrdersFileName,
                String::compareTo,
                String::compareTo
        ).getFuture();

        this.usersAndProducts = futureStorageFactory.create(
                usersAndProductsFileName,
                String::compareTo,
                String::compareTo
        ).getFuture();

        this.productsAndUsers = futureStorageFactory.create(
                productsAndUsersFileName,
                String::compareTo,
                String::compareTo
        ).getFuture();
    }

    @Override
    public CompletableFuture<Boolean> isValidOrderId(String orderId) {
        return ordersAndProducts
                .thenCompose(futureStorage -> futureStorage.existsBySingleId(orderId));
    }

    @Override
    public CompletableFuture<Boolean> isCanceledOrder(String orderId) {
        return ordersAndProducts
                .thenCompose(futureStorage -> futureStorage.getSomeStringBySingleId(orderId))
                .thenApply(optionalString ->
                        optionalString
                                .map(s -> new Order(removeKey(s)).isCancelled())
                                .orElse(false)
                );
    }

    @Override
    public CompletableFuture<Boolean> isModifiedOrder(String orderId) {
        return ordersAndProducts
                .thenCompose(futureStorage -> futureStorage.getSomeStringBySingleId(orderId))
                .thenApply(optionalString ->
                        optionalString
                                .map(s -> new Order(removeKey(s)).isModified())
                                .orElse(false)
                );
    }

    @Override
    public CompletableFuture<OptionalInt> getNumberOfProductOrdered(String orderId) {
        return ordersAndProducts
                .thenCompose(futureStorage -> futureStorage.getSomeStringBySingleId(orderId))
                .thenApply(optionalString ->
                        optionalString
                                .map(s -> {
                                    Order orderFound = new Order(removeKey(s));
                                    return OptionalInt.of(
                                            (orderFound.isCancelled()? -1 : 1) * orderFound.getLatestAmount()
                                    );
                                })
                                .orElse(OptionalInt.empty())
                );
    }

    @Override
    public CompletableFuture<List<Integer>> getHistoryOfOrder(String orderId) {
        return ordersAndHistory
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(orderId))
                .thenApply(
                        list -> list
                                .stream()
                                .map(this::removeKey)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList())
                );
    }

    @Override
    public CompletableFuture<List<String>> getOrderIdsForUser(String userId) {
        return usersAndOrders
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(userId))
                .thenApply(
                        list -> list
                                .stream()
                                .map(this::removeKey)
                                .map(Order::new)
                                .map(Order::getOrderId)
                                .collect(Collectors.toList())
                );
    }

    @Override
    public CompletableFuture<Long> getTotalAmountSpentByUser(String userId) {
        return usersAndProducts
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(userId))
                .thenApply(
                        list -> list
                                .stream()
                                .map(this::removeKey)
                                .map(Order::new)
                                .mapToLong(order -> order.getLatestAmount() * order.getProductPrice())
                                .sum()
                );
    }

    @Override
    public CompletableFuture<List<String>> getUsersThatPurchased(String productId) {
        return productsAndUsers
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(productId))
                .thenApply(
                        list -> list
                                .stream()
                                .map(this::removeKey)
                                .map(Order::new)
                                .map(Order::getUserId)
                                .collect(Collectors.toList())
                );
    }

    @Override
    public CompletableFuture<List<String>> getOrderIdsThatPurchased(String productId) {
        return productsAndOrders
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(productId))
                .thenApply(
                        list -> list
                                .stream()
                                .map(this::removeKey)
                                .map(Order::new)
                                .map(Order::getOrderId)
                                .collect(Collectors.toList())
                );
    }

    @Override
    public CompletableFuture<OptionalLong> getTotalNumberOfItemsPurchased(String productId) {
        return productsAndPrices
                .thenCompose(futureStorage -> futureStorage.existsBySingleId(productId))
                .thenCombine(
                        productsAndUsers.thenCompose(futureStorage -> futureStorage.getAllStringsById(productId)),
                        (exists, list) -> {
                            if (exists) {
                                return OptionalLong.of(
                                        list.stream()
                                                .map(this::removeKey)
                                                .map(Order::new)
                                                .mapToLong(Order::getLatestAmount)
                                                .sum()
                                );
                            } else {
                                return OptionalLong.empty();
                            }
                });
    }

    @Override
    public CompletableFuture<OptionalDouble> getAverageNumberOfItemsPurchased(String productId) {
        return productsAndPrices
                .thenCompose(futureStorage -> futureStorage.existsBySingleId(productId))
                .thenCombine(
                        productsAndOrders.thenCompose(futureStorage -> futureStorage.getAllStringsById(productId)),
                        (exists, list) -> {
                            if (exists) {
                                return list.stream()
                                        .map(this::removeKey)
                                        .map(Order::new)
                                        .filter(order -> !order.isCancelled())
                                        .mapToDouble(Order::getLatestAmount)
                                        .average();
                            } else {
                                return OptionalDouble.empty();
                            }
                        });
    }

    @Override
    public CompletableFuture<OptionalDouble> getCancelRatioForUser(String userId) {
        return usersAndOrders
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(userId))
                .thenApply(list -> list.stream()
                        .map(this::removeKey)
                        .map(Order::new)
                        .map(Order::isCancelled)
                        .mapToDouble(isCanceled -> (isCanceled ? 1 : 0))
                        .average());
    }

    @Override
    public CompletableFuture<OptionalDouble> getModifyRatioForUser(String userId) {
        return usersAndOrders
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(userId))
                .thenApply(list -> list.stream()
                        .map(this::removeKey)
                        .map(Order::new)
                        .map(Order::isModified)
                        .mapToDouble(isModified -> (isModified ? 1 : 0))
                        .average());
    }

    @Override
    public CompletableFuture<Map<String, Long>> getAllItemsPurchased(String userId) {
        return usersAndProducts
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(userId))
                .thenApply(list -> list.stream()
                        .map(this::removeKey)
                        .map(Order::new)
                        .collect(Collectors.toMap(
                                Order::getProductId,
                                o -> Long.valueOf(o.getLatestAmount())
                        ))
                );
    }

    @Override
    public CompletableFuture<Map<String, Long>> getItemsPurchasedByUsers(String productId) {
        return productsAndUsers
                .thenCompose(futureStorage -> futureStorage.getAllStringsById(productId))
                .thenApply(list -> list.stream()
                        .map(this::removeKey)
                        .map(Order::new)
                        .collect(Collectors.toMap(
                                Order::getUserId,
                                o -> Long.valueOf(o.getLatestAmount())
                        ))
                );
    }

    private String removeKey(String s) {
        return s.split(",", 3)[2];
    }
}
