package il.ac.technion.cs.sd.buy.app;

import java.util.ArrayList;
import java.util.List;

public class Order{
    private String orderId;
    private String userId;
    private String productId;
    private Integer productPrice;
    private Integer latestAmount;
    private boolean isCancelled;
    private boolean isModified;
    private List<Integer> amountHistory = new ArrayList<>();

    public Order(String orderId, String userId, String productId, Integer initialAmount, Integer productPrice) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.latestAmount = initialAmount;
        this.productPrice = productPrice;

        this.isCancelled = false;
        this.isModified = false;
        amountHistory.add(initialAmount);
    }

    public Order(String csvString) {
        String[] splitString = csvString.split(",");
        this.orderId = splitString[0];
        this.userId = splitString[1];
        this.productId = splitString[2];
        this.latestAmount = Integer.parseInt(splitString[3]);
        this.productPrice = Integer.parseInt(splitString[4]);
        this.isCancelled = stringToBoolean(splitString[5]);
        this.isModified = stringToBoolean(splitString[6]);

        amountHistory.add(latestAmount);
    }

    @Override
    public String toString() {
        return String.join(
                ",",
                orderId,
                userId,
                productId,
                latestAmount.toString(),
                productPrice.toString(),
                serializeBoolean(isCancelled()),
                serializeBoolean(isModified())
        );
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public Integer getLatestAmount() {
        return latestAmount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isModified() {
        return isModified || amountHistory.size() > 1;
    }

    public List<Integer> getAmountHistory() {
        return amountHistory;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public void modifyAmount(Integer newAmount) {
        latestAmount = newAmount;
        amountHistory.add(newAmount);
        setCancelled(false);
    }

    private String serializeBoolean(Boolean b) {
        return b ? "1" : "0";
    }

    private Boolean stringToBoolean(String s) {
        return s.equals("1");
    }
}
