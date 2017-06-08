package il.ac.technion.cs.sd.buy.app;

import java.util.ArrayList;
import java.util.List;

class Order {
    String orderId;
    String userId;
    String productId;

    //TODO IDO - can JSON notation of number contain leading zero? ask SHAI
    Integer initialAmount;

    boolean isCancelled;
    List<Integer> modificationsAmount;

    public Order(String orderId, String userId, String productId, Integer initialAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.initialAmount = initialAmount;

        this.isCancelled = false;
        modificationsAmount = new ArrayList<>();
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

    public Integer getInitialAmount() {
        return initialAmount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public List<Integer> getModificationsAmount() {
        return modificationsAmount;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public void addModification(Integer amount) {
        modificationsAmount.add(amount);
        setCancelled(false);
    }
}
