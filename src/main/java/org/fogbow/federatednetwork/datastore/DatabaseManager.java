package org.fogbow.federatednetwork.datastore;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.ConfigurationConstants;
import org.fogbow.federatednetwork.datastore.order_storage.RecoveryService;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    public Set<FederatedOrder> readActiveFederatedNetworks(FederationUserToken user) {
        return null;
    }

    @Override
    public ConcurrentHashMap<String, Set<FederatedOrder>> retrieveActiveFederatedNetworks() {
        throw new UnsupportedOperationException();
    }
}
