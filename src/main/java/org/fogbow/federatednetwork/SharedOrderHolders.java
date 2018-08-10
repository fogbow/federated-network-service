package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.RedirectedComputeOrder;

import java.util.Map;

public class SharedOrderHolders {

    private static SharedOrderHolders instance;

    private Map<String, FederatedNetworkOrder> activeFederatedNetworks;
    private Map<String, RedirectedComputeOrder> activeRedirectedComputes;

    private SharedOrderHolders() {
        // retrieve from database
    }

    public static synchronized SharedOrderHolders getInstance() {
        if (instance == null) {
            instance = new SharedOrderHolders();
        }
        return instance;
    }

    public Map<String, FederatedNetworkOrder> getActiveFederatedNetworks() {
        return activeFederatedNetworks;
    }

    public Map<String, RedirectedComputeOrder> getActiveRedirectedComputes() {
        return activeRedirectedComputes;
    }
}
