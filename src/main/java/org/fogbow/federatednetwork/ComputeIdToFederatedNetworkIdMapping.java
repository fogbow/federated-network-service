package org.fogbow.federatednetwork;

import java.util.HashMap;
import java.util.Map;

public class ComputeIdToFederatedNetworkIdMapping {
    private static ComputeIdToFederatedNetworkIdMapping instance;

    private Map<String, String> computeIdToFederatedNetworkIdMap;

    private ComputeIdToFederatedNetworkIdMapping() {
        this.computeIdToFederatedNetworkIdMap = new HashMap<>();
    }

    public static synchronized ComputeIdToFederatedNetworkIdMapping getInstance() {
        if (instance == null) {
            instance = new ComputeIdToFederatedNetworkIdMapping();
        }
        return instance;
    }

    public String put(String computeId, String federatedNetworkId) {
        return computeIdToFederatedNetworkIdMap.put(computeId, federatedNetworkId);
    }

    public String get(String computeId) {
        return computeIdToFederatedNetworkIdMap.get(computeId);
    }

    public String remove(String computeId) {
        return computeIdToFederatedNetworkIdMap.remove(computeId);
    }
}
