package il.ac.technion.cs.sd.buy.app;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Parser {
    protected SortedMap<String, String> products = new TreeMap<>();
    protected SortedMap<String, Order> orders = new TreeMap<>();

    public SortedMap<String, String> getProducts() {
        return products;
    }

    public SortedMap<String, Order> getOrders() {
        return orders;
    }

    public void print() {
        System.out.println("Products");
        for (Map.Entry<String,String> entry : products.entrySet()){
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println("Orders");
        for (Map.Entry<String,Order> entry : orders.entrySet()){
            System.out.println("orderId " + entry.getKey() + " userId " + entry.getValue().getUserId() + " productID "
                    + entry.getValue().getProductId() + " latestAmount " + entry.getValue().getLatestAmount());
            System.out.print("amount history: ");
            for(Integer amount : entry.getValue().getAmountHistory()){
                System.out.print(amount + " ");
            }
            System.out.println("cancelled: " + entry.getValue().isCancelled());
        }
    }
}
