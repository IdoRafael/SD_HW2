package il.ac.technion.cs.sd.buy.app;

import java.util.ArrayList;
import java.util.List;

public class Order{
    private String orderId;
    private String userId;
    private String productId;
    private Integer latestAmount;
    private boolean isCancelled;
    private List<Integer> amountHistory;

    public Order(String orderId, String userId, String productId, Integer initialAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.latestAmount = initialAmount;

        this.isCancelled = false;
        amountHistory = new ArrayList<>();
        amountHistory.add(initialAmount);
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

    public Integer getLatestAmount() {
        return latestAmount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isModified() {
        return amountHistory.size() > 1;
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
}
