package cloud.fogbow.fns.core;

import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.core.datastore.DatabaseManager;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;

import java.util.HashMap;
import java.util.Map;

public class FederatedNetworkOrdersHolder {

    private static FederatedNetworkOrdersHolder instance;

    private Map<String, FederatedNetworkOrder> activeOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> openOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> fulfilledOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> failedOrders;

    private FederatedNetworkOrdersHolder() {
        // retrieve from database
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        this.openOrders = databaseManager.readActiveOrders(OrderState.OPEN);
        this.fulfilledOrders = databaseManager.readActiveOrders(OrderState.FULFILLED);
        this.failedOrders = databaseManager.readActiveOrders(OrderState.FAILED);

        // initialize activeOrders
        this.activeOrders = new HashMap<>();
        addAll(this.activeOrders, this.openOrders, this.fulfilledOrders, this.failedOrders);
    }

    public static synchronized FederatedNetworkOrdersHolder getInstance() {
        if (instance == null) {
            instance = new FederatedNetworkOrdersHolder();
        }
        return instance;
    }

    public Map<String, FederatedNetworkOrder> getActiveOrders() {
        return this.activeOrders;
    }

    public FederatedNetworkOrder insertNewOrder(FederatedNetworkOrder order) {
        openOrders.addItem(order);
        return activeOrders.put(order.getId(), order);
    }

    public FederatedNetworkOrder getOrder(String id) {
        return activeOrders.get(id);
    }

    public FederatedNetworkOrder getFederatedNetworkOrder(String id) {
        FederatedNetworkOrder order = activeOrders.get(id);
        return order;
    }

    public FederatedNetworkOrder removeOrder(String id) {
        FederatedNetworkOrder order = activeOrders.get(id);
        SynchronizedDoublyLinkedList<FederatedNetworkOrder> list = getOrdersList(order.getOrderState());
        list.removeItem(order);
        return activeOrders.remove(id);
    }

    public FederatedNetworkOrder removeOrder(Order order) {
        return removeOrder(order.getId());
    }

    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> getOrdersList(OrderState orderState) {
        switch (orderState) {
            case OPEN:
                return this.openOrders;
            case FULFILLED:
                return this.fulfilledOrders;
            case FAILED:
                return this.failedOrders;
            default:
                return null;
        }
    }

    private void addAll(Map<String, FederatedNetworkOrder> activeOrders,
                        SynchronizedDoublyLinkedList<FederatedNetworkOrder>... listsToBeAdded) {
        FederatedNetworkOrder order;

        for (SynchronizedDoublyLinkedList<FederatedNetworkOrder> listToBeAdded : listsToBeAdded) {
            while ((order = listToBeAdded.getNext()) != null) {
                activeOrders.put(order.getId(), order);
            }
            listToBeAdded.resetPointer();
        }
    }
}
