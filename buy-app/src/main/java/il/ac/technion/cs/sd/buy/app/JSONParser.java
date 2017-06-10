package il.ac.technion.cs.sd.buy.app;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Yaniv on 09/06/2017.
 */
public class JSONParser {
    private SortedMap<String, String> products = new TreeMap<>();
    private SortedMap<String, Order> orders = new TreeMap<>();

    public JSONParser(String json){
        JsonElement jsonElement = new JsonParser().parse(json);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        parseProducts(jsonArray);
        parseOrders(jsonArray);
    }

    public SortedMap<String, String> getProducts() {
        return products;
    }

    public SortedMap<String, Order> getOrders() {
        return orders;
    }

    private void parseProducts(JsonArray jsonArray){
        for (JsonElement element : jsonArray){
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.get("type").getAsString().equals("product")){
                products.put(jsonObject.get("id").getAsString(), jsonObject.get("price").getAsString());
            }
        }
    }

    private void parseOrders(JsonArray jsonArray){
        for (JsonElement element : jsonArray){
            JsonObject jsonObject = element.getAsJsonObject();
            switch (jsonObject.get("type").getAsString()) {
                case "order":
                    handleOrder(jsonObject);
                    break;
                case "modify-order":
                    modifyOrder(jsonObject);
                    break;
                case "cancel-order":
                    cancelOrder(jsonObject);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleOrder(JsonObject jsonObject){
        String productId = jsonObject.get("product-id").getAsString();
        if (!products.containsKey(productId)) {
            return;
        }
        String userId = jsonObject.get("user-id").getAsString();
        String orderId = jsonObject.get("order-id").getAsString();
        int amount = jsonObject.get("amount").getAsInt();
        orders.put(orderId, new Order(orderId, userId, productId, amount));
    }

    private void modifyOrder(JsonObject jsonObject){
        String orderId = jsonObject.get("order-id").getAsString();
        if (!orders.containsKey(orderId)){
            return;
        }
        int newAmount = jsonObject.get("amount").getAsInt();
        //no need to cancel, modifyAmount does this already
        //orders.get(orderId).setCancelled(false);
        orders.get(orderId).modifyAmount(newAmount);
    }

    private void cancelOrder(JsonObject jsonObject){
        String orderId = jsonObject.get("order-id").getAsString();
        if (!orders.containsKey(orderId)) {
            return;
        }
        orders.get(orderId).setCancelled(true);
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
