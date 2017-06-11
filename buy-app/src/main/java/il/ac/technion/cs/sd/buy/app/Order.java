package il.ac.technion.cs.sd.buy.app;

import java.util.ArrayList;
import java.util.List;

public class Order{
    private String orderId;
    private String userId;
    private String productId;
    private Long productPrice;
    private Long latestAmount;
    private boolean isCancelled;
    private boolean isModified;
    private List<Long> amountHistory = new ArrayList<>();

    public Order(String orderId, String userId, String productId, Long initialAmount, Long productPrice) {
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
        this.latestAmount = Long.parseLong(splitString[3]);
        this.productPrice = Long.parseLong(splitString[4]);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Order otherOrder = (Order) obj;
        return orderId.equals(otherOrder.orderId)
                && userId.equals(otherOrder.userId)
                && productId.equals(otherOrder.productId)
                && productPrice.equals(otherOrder.productPrice)
                && latestAmount.equals(otherOrder.latestAmount)
                && isCancelled == otherOrder.isCancelled
                && isModified == otherOrder.isModified
                && amountHistory.equals(otherOrder.amountHistory);
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

    public Long getProductPrice() {
        return productPrice;
    }

    public Long getLatestAmount() {
        return latestAmount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isModified() {
        return isModified || amountHistory.size() > 1;
    }

    public List<Long> getAmountHistory() {
        return amountHistory;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public void modifyAmount(Long newAmount) {
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
