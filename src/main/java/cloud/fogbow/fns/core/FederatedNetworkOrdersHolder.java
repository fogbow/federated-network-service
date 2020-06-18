package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.core.datastore.DatabaseManager;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FederatedNetworkOrdersHolder {
    private static FederatedNetworkOrdersHolder instance;
    private Map<String, FederatedNetworkOrder> activeOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> openOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> spawningOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> partiallyFulfilledOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> fulfilledOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> failedOrders;
    private SynchronizedDoublyLinkedList<FederatedNetworkOrder> closedOrders;

    private FederatedNetworkOrdersHolder() throws InternalServerErrorException {
        // retrieve from database
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        this.openOrders = databaseManager.readActiveOrders(OrderState.OPEN);
        this.spawningOrders = databaseManager.readActiveOrders(OrderState.SPAWNING);
        this.fulfilledOrders = databaseManager.readActiveOrders(OrderState.FULFILLED);
        this.partiallyFulfilledOrders = databaseManager.readActiveOrders(OrderState.PARTIALLY_FULFILLED);
        this.failedOrders = databaseManager.readActiveOrders(OrderState.FAILED);
        this.closedOrders = databaseManager.readActiveOrders(OrderState.CLOSED);

        this.activeOrders = initializeActiveOrders(this.openOrders, this.spawningOrders, this.fulfilledOrders, this.partiallyFulfilledOrders, this.failedOrders, this.closedOrders);
    }

    public static synchronized FederatedNetworkOrdersHolder getInstance() throws InternalServerErrorException {
        if (instance == null) {
            instance = new FederatedNetworkOrdersHolder();
        }
        return instance;
    }

    public Map<String, FederatedNetworkOrder> getActiveOrders() {
        return this.activeOrders;
    }

    public FederatedNetworkOrder putOrder(FederatedNetworkOrder order) throws InternalServerErrorException {
        getOrdersList(order.getOrderState()).addItem(order);
        return activeOrders.put(order.getId(), order);
    }

    public FederatedNetworkOrder getOrder(String id) {
        return activeOrders.get(id);
    }

    public FederatedNetworkOrder removeOrder(FederatedNetworkOrder order) throws InternalServerErrorException {
        return removeOrder(order.getId());
    }

    public FederatedNetworkOrder removeOrder(String id) throws InternalServerErrorException {
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
            case SPAWNING:
                return this.spawningOrders;
            case PARTIALLY_FULFILLED:
                return this.partiallyFulfilledOrders;
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

    private Map<String, FederatedNetworkOrder> initializeActiveOrders(SynchronizedDoublyLinkedList<FederatedNetworkOrder>... listsToBeAdded) {
        Map<String, FederatedNetworkOrder> allOrders = new ConcurrentHashMap<>();
        FederatedNetworkOrder order;

        for (SynchronizedDoublyLinkedList<FederatedNetworkOrder> listToBeAdded : listsToBeAdded) {
            while ((order = listToBeAdded.getNext()) != null) {
                allOrders.put(order.getId(), order);
            }
            listToBeAdded.resetPointer();
        }
        return allOrders;
    }
}
