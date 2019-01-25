package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

import java.util.Map;

public class FederatedNetworkOrdersHolder {

    private static FederatedNetworkOrdersHolder instance;

    private Map<String, FederatedNetworkOrder> activeOrdersMap;

    private FederatedNetworkOrdersHolder() {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        this.activeOrdersMap = databaseManager.retrieveActiveFederatedOrders();
        // retrieve from database
    }

    public static synchronized FederatedNetworkOrdersHolder getInstance() {
        if (instance == null) {
            instance = new FederatedNetworkOrdersHolder();
        }
        return instance;
    }

    public Map<String, FederatedNetworkOrder> getActiveOrdersMap() {
        return this.activeOrdersMap;
    }

    public FederatedNetworkOrder putOrder(FederatedNetworkOrder order) {
        return activeOrdersMap.put(order.getId(), order);
    }

    public FederatedNetworkOrder getOrder(String id) {
        return activeOrdersMap.get(id);
    }

    public FederatedNetworkOrder getFederatedNetworkOrder(String id) {
        FederatedNetworkOrder order = activeOrdersMap.get(id);
        return order;
    }

    public FederatedNetworkOrder removeOrder(String id) {
        return activeOrdersMap.remove(id);
    }
}
