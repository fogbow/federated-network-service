package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.UnexpectedException;
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
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> closedOrders;

    private FederatedNetworkOrdersHolder() {
        // retrieve from database
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        this.openOrders = databaseManager.readActiveOrders(OrderState.OPEN);
        this.fulfilledOrders = databaseManager.readActiveOrders(OrderState.FULFILLED);
        this.failedOrders = databaseManager.readActiveOrders(OrderState.FAILED);
        this.closedOrders = databaseManager.readActiveOrders(OrderState.CLOSED);

        this.activeOrders = initializeActiveOrders(this.openOrders, this.fulfilledOrders, this.failedOrders, this.closedOrders);
    }

    public static synchronized FederatedNetworkOrdersHolder getInstance() {
        if (instance == null) {
            instance = new FederatedNetworkOrdersHolder();
        }
        return instance;
    }

    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> getOpenOrders() {
        return this.openOrders;
    }

    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> getFulfilledOrders() {
        return this.fulfilledOrders;
    }

    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> getFailedOrders() {
        return this.failedOrders;
    }

    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> getClosedOrders() {
        return this.closedOrders;
    }

    public Map<String, FederatedNetworkOrder> getActiveOrders() {
        return this.activeOrders;
    }

    public FederatedNetworkOrder insertNewOrder(FederatedNetworkOrder order) {
        getOrdersList(order.getOrderState()).addItem(order);
        return activeOrders.put(order.getId(), order);
    }

    public FederatedNetworkOrder getOrder(String id) {
        return activeOrders.get(id);
    }

    public FederatedNetworkOrder getFederatedNetworkOrder(String id) {
        FederatedNetworkOrder order = activeOrders.get(id);
        return order;
    }

    public FederatedNetworkOrder removeOrder(FederatedNetworkOrder order) throws UnexpectedException {
        return removeOrder(order.getId());
    }

    public FederatedNetworkOrder removeOrder(String id) throws UnexpectedException {
        FederatedNetworkOrder order = activeOrders.get(id);

        getOrdersList(order.getOrderState()).removeItem(order);
        FederatedNetworkOrder removedOrder = activeOrders.remove(id);

        order.setOrderState(OrderState.DEACTIVATED);
        return removedOrder;
    }

    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> getOrdersList(OrderState orderState) {
        switch (orderState) {
            case OPEN:
                return this.openOrders;
            case FULFILLED:
                return this.fulfilledOrders;
            case FAILED:
                return this.failedOrders;
            case CLOSED:
                return this.closedOrders;
            default:
                return null;
        }
    }

    private HashMap<String, FederatedNetworkOrder> initializeActiveOrders(SynchronizedDoublyLinkedList<FederatedNetworkOrder>... listsToBeAdded) {
        HashMap<String, FederatedNetworkOrder> allOrders = new HashMap();
        FederatedNetworkOrder order;

        for (SynchronizedDoublyLinkedList<FederatedNetworkOrder> listToBeAdded : listsToBeAdded) {
            while ((order = listToBeAdded.getNext()) != null) {
                activeOrders.put(order.getId(), order);
            }
            listToBeAdded.resetPointer();
        }
        return allOrders;
    }
}
