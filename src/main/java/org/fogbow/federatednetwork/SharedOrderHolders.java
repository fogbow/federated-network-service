package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedOrder;

import java.sql.SQLException;
import java.util.Map;

public class SharedOrderHolders {

    private static SharedOrderHolders instance;

    private Map<String, FederatedOrder> activeOrdersMap;

    private SharedOrderHolders() throws SubnetAddressesCapacityReachedException, InvalidCidrException, SQLException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        this.activeOrdersMap = databaseManager.retrieveActiveFederatedOrders();
        // retrieve from database
    }

    public static synchronized SharedOrderHolders getInstance() throws SubnetAddressesCapacityReachedException,
            InvalidCidrException, SQLException {
        if (instance == null) {
            instance = new SharedOrderHolders();
        }
        return instance;
    }

    public Map<String, FederatedOrder> getActiveOrdersMap() {
        return this.activeOrdersMap;
    }

    public void setActiveOrdersMap(Map<String, FederatedOrder> activeOrdersMap) {
        this.activeOrdersMap = activeOrdersMap;
    }

    public FederatedOrder putOrder(FederatedOrder order) {
        return activeOrdersMap.put(order.getId(), order);
    }

    public FederatedOrder getOrder(String id) {
        return activeOrdersMap.get(id);
    }

    public FederatedNetworkOrder getFederatedNetwork(String id) {
        FederatedOrder order = activeOrdersMap.get(id);
        if (order instanceof FederatedNetworkOrder) {
            return (FederatedNetworkOrder) order;
        }
        return null;
    }

    public FederatedComputeOrder getFederatedCompute(String id) {
        FederatedOrder order = activeOrdersMap.get(id);
        if (order instanceof FederatedComputeOrder) {
            return (FederatedComputeOrder) order;
        }
        return null;
    }

    public FederatedOrder removeOrder(String id) {
        return activeOrdersMap.remove(id);
    }
}
