package org.fogbow.federatednetwork.datastore;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.datastore.order_storage.RecoveryService;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.manager.core.models.linkedlists.SynchronizedDoublyLinkedList;
import org.fogbowcloud.manager.core.models.orders.OrderState;

import java.util.Map;
import java.util.Properties;

public class DatabaseManager implements StableStorage {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;
    private RecoveryService recoveryService;

    private DatabaseManager() {
        Properties properties = PropertiesUtil.readProperties();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void setRecoveryService(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @Override
    public void put(FederatedOrder federatedOrder) {
        recoveryService.put(federatedOrder);
    }

    @Override
    public SynchronizedDoublyLinkedList readActiveFederatedNetworks(OrderState orderState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, FederatedOrder> retrieveActiveFederatedNetworks() {
        return recoveryService.readActiveOrders();
    }
}
