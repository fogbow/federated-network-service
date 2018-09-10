package org.fogbow.federatednetwork.datastore;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.datastore.order_storage.OrderTimestampStorage;
import org.fogbow.federatednetwork.datastore.order_storage.RecoveryService;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbowcloud.ras.core.models.linkedlists.SynchronizedDoublyLinkedList;
import org.fogbowcloud.ras.core.models.orders.OrderState;

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
    public void put(FederatedOrder federatedOrder) {
        try {
            recoveryService.put(federatedOrder);
            if (federatedOrder instanceof FederatedNetworkOrder) {
                orderTimestampStorage.addOrder(federatedOrder);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SynchronizedDoublyLinkedList readActiveFederatedNetworks(OrderState orderState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, FederatedOrder> retrieveActiveFederatedOrders() throws SubnetAddressesCapacityReachedException,
            InvalidCidrException {
        return recoveryService.readActiveOrders();
    }

    public void setRecoveryService(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }
}
