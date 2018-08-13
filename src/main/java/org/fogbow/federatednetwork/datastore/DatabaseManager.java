package org.fogbow.federatednetwork.datastore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.ConfigurationConstants;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class DatabaseManager implements StableStorage {

    private static final String USER_TO_FEDERATED_COMPUTES = "userToFederatedComputes";
    private static final String USER_TO_FEDERATED_NETWORKS = "userToFederatedNetworks";
    private static final String ERROR_MASSAGE = "Error instantiating database manager";
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);
    private static final Gson gson = new Gson();

    private final String databaseFilePath;

    public DatabaseManager() {
        Properties properties = PropertiesUtil.readProperties();
        this.databaseFilePath = properties.getProperty(ConfigurationConstants.DATABASE_FILE_PATH);
    }

    private DB openDatabase() {
        return DBMaker.fileDB(new File(databaseFilePath)).make();
    }

    // Federated network methods

    @Override
    public void putFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder, FederationUser user) {
        DB database = openDatabase();

        try {
            HTreeMap<String, String> userIdToFederatedNetworks = extractFederatedNetworksMap(database);

            Set<FederatedNetworkOrder> federatedNetworks = getFederatedNetworks(userIdToFederatedNetworks,
                    user);
            if (federatedNetworks.contains(federatedNetworkOrder)) {
                federatedNetworks.remove(federatedNetworkOrder);
            }
            federatedNetworks.add(federatedNetworkOrder);
            userIdToFederatedNetworks.put(user.getId(), gson.toJson(federatedNetworks));
        } finally {
            database.commit();
            database.close();
        }
    }

    @Override
    public void deleteFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder, FederationUser user) {
        DB database = openDatabase();
        HTreeMap<String, String> userIdToFederatedNetworks = extractFederatedNetworksMap(database);

        try {
            Set<FederatedNetworkOrder> federatedNetworks = getFederatedNetworks(userIdToFederatedNetworks,
                    user);
            if (federatedNetworks.contains(federatedNetworkOrder)) {
                federatedNetworks.remove(federatedNetworkOrder);
            } else {
                // throw an exception
            }
            userIdToFederatedNetworks.put(user.getId(), gson.toJson(federatedNetworks));
        } finally {
            database.commit();
            database.close();
        }
    }

    @Override
    public Set<FederatedNetworkOrder> readActiveFederatedNetworks(FederationUser user) {
        DB database = openDatabase();
        HTreeMap<String, String> userIdToFederatedNetworks = extractFederatedNetworksMap(database);

        try {
            return getFederatedNetworks(userIdToFederatedNetworks, user);
        } finally {
            database.close();
        }
    }

    private HTreeMap<String, String> extractFederatedNetworksMap(DB database) {
        /* The keys for this map are the userId's and the values are
         * JSONArrays representing the networks for this user */
        DB.HashMapMaker<String, String> userToFedNetworks = database.hashMap(
                USER_TO_FEDERATED_NETWORKS, Serializer.STRING, Serializer.STRING);
        return userToFedNetworks.createOrOpen();
    }

    private Set<FederatedNetworkOrder> getFederatedNetworks(HTreeMap<String, String> userIdToFedNetworks, FederationUser user) {
        Set<FederatedNetworkOrder> federatedNetworks;
        if (userIdToFedNetworks.containsKey(user.getId())) {
            String jsonNetworks = userIdToFedNetworks.get(user.getId());
            federatedNetworks = parseFederatedNetworks(jsonNetworks);
        } else {
            federatedNetworks = new HashSet<>();
        }

        return federatedNetworks;
    }

    protected Set<FederatedNetworkOrder> parseFederatedNetworks(String jsonArray) {
        Type listType = new TypeToken<Set<FederatedNetworkOrder>>() {
        }.getType();
        Set<FederatedNetworkOrder> federatedNetworks = gson.fromJson(jsonArray, listType);
        return federatedNetworks;
    }

    // compute methods

    @Override
    public void putFederatedCompute(FederatedComputeOrder federatedComputeOrder, FederationUser user) {
        DB database = openDatabase();
        HTreeMap<String, String> userIdToFederatedComputes = extractFederatedComputesMap(database);

        Set<FederatedComputeOrder> federatedComputes = getFederatedComputes(userIdToFederatedComputes, user);
        if (federatedComputes.contains(federatedComputeOrder)) {
            federatedComputes.remove(federatedComputeOrder);
        }
        federatedComputes.add(federatedComputeOrder);
        userIdToFederatedComputes.put(user.getId(), gson.toJson(federatedComputes));

        database.commit();
        database.close();
    }

    @Override
    public void deleteFederatedCompute(FederatedComputeOrder federatedComputeOrder, FederationUser user) {
        DB database = openDatabase();
        HTreeMap<String, String> userIdToFederatedComputes = extractFederatedComputesMap(database);

        try {
            Set<FederatedComputeOrder> federatedComputes = getFederatedComputes(userIdToFederatedComputes, user);
            if (federatedComputes.contains(federatedComputeOrder)) {
                federatedComputes.remove(federatedComputeOrder);
            } else {
                // throw an exception
            }
            userIdToFederatedComputes.put(user.getId(), gson.toJson(federatedComputes));
        } finally {
            database.commit();
            database.close();
        }
    }

    @Override
    public Set<FederatedComputeOrder> readActiveFederatedComputes(FederationUser user) {
        DB database = openDatabase();
        HTreeMap<String, String> userIdToFederatedComputes = extractFederatedComputesMap(database);

        try {
            return getFederatedComputes(userIdToFederatedComputes, user);
        } finally {
            database.close();
        }
    }

    private Set<FederatedComputeOrder> getFederatedComputes(HTreeMap<String, String> userIdToFedNetworks, FederationUser user) {
        Set<FederatedComputeOrder> federatedComputes;
        if (userIdToFedNetworks.containsKey(user.getId())) {
            String jsonNetworks = userIdToFedNetworks.get(user.getId());
            federatedComputes = parseFederatedComputes(jsonNetworks);
        } else {
            federatedComputes = new HashSet<>();
        }

        return federatedComputes;
    }

    private HTreeMap<String, String> extractFederatedComputesMap(DB database) {
        /* The keys for this map are the userId's and the values are
         * JSONArrays representing the computes for this user */
        DB.HashMapMaker<String, String> userToFedNetworks = database.hashMap(
                USER_TO_FEDERATED_COMPUTES, Serializer.STRING, Serializer.STRING);
        return userToFedNetworks.createOrOpen();
    }

    protected Set<FederatedComputeOrder> parseFederatedComputes(String jsonArray) {
        Type listType = new TypeToken<Set<FederatedComputeOrder>>() {
        }.getType();
        Set<FederatedComputeOrder> federatedComputes = gson.fromJson(jsonArray, listType);
        return federatedComputes;
    }
}
