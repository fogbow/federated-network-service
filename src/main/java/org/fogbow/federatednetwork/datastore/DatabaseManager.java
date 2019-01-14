package org.fogbow.federatednetwork.datastore;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.datastore.orderstorage.AuditService;
import org.fogbow.federatednetwork.datastore.orderstorage.RecoveryService;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class DatabaseManager implements StableStorage {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;

    @Autowired
    private RecoveryService recoveryService;

    @Autowired
    private AuditService auditService;

    private DatabaseManager() {
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    @Override
    public void put(FederatedNetworkOrder order) {
        recoveryService.put(order);
        auditService.updateStateTimestamp(order);
    }

    @Override
    public Map<String, FederatedNetworkOrder> retrieveActiveFederatedOrders() {
        return recoveryService.readActiveOrders();
    }

    public void setRecoveryService(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

}
