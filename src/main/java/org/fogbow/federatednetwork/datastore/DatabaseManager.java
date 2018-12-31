package org.fogbow.federatednetwork.datastore;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.datastore.orderstorage.OrderTimestampStorage;
import org.fogbow.federatednetwork.datastore.orderstorage.RecoveryService;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

import java.sql.SQLException;
import java.util.Map;

public class DatabaseManager implements StableStorage {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;
    private RecoveryService recoveryService;
    private OrderTimestampStorage orderTimestampStorage;

    private DatabaseManager() throws SQLException {
        this.orderTimestampStorage = new OrderTimestampStorage();
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    @Override
    public void put(FederatedNetworkOrder order) {
        try {
            recoveryService.put(order);
            orderTimestampStorage.addOrder(order);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, FederatedNetworkOrder> retrieveActiveFederatedOrders() {
        return recoveryService.readActiveOrders();
    }

    public void setRecoveryService(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }
}
