package org.fogbow.federatednetwork.datastore;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.ConfigurationConstants;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager implements StableStorage {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);

    private final String databaseFilePath;
    private static DatabaseManager instance;

    private DatabaseManager() {
        Properties properties = PropertiesUtil.readProperties();
        this.databaseFilePath = properties.getProperty(ConfigurationConstants.DATABASE_FILE_PATH);
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private DB openDatabase() {
        return DBMaker.fileDB(new File(databaseFilePath)).make();
    }

    @Override
    public void put(FederatedOrder federatedOrder) {

    }

    @Override
    public void delete(FederatedOrder federatedOrder) {

    }

    @Override
    public Set<FederatedOrder> readActiveFederatedNetworks(FederationUserToken user) {
        return null;
    }

    public ConcurrentHashMap<String, Set<FederatedOrder>> retrieveActiveFederatedNetworks() {
        throw new UnsupportedOperationException();
    }
}
