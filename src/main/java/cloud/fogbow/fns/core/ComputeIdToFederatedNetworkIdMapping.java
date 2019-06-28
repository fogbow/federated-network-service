package cloud.fogbow.fns.core;

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

    public synchronized String put(String computeId, String federatedNetworkId) {
        return computeIdToFederatedNetworkIdMap.put(computeId, federatedNetworkId);
    }

    public synchronized String get(String computeId) {
        return computeIdToFederatedNetworkIdMap.get(computeId);
    }

    public synchronized String remove(String computeId) {
        return computeIdToFederatedNetworkIdMap.remove(computeId);
    }
}
