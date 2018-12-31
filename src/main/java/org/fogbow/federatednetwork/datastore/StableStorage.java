package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

import java.util.Map;

public interface StableStorage {

    /**
     * Add or update the federatedNetworkOrder into database, so we can recovery it when necessary.
     * @param order {@link FederatedNetworkOrder}
     */
    public void put(FederatedNetworkOrder order);

    /**
     * Retrieve all federated networks, may be interest to create a database recovery
     * @return A map of user id to federated order {@link FederatedNetworkOrder}
     */
    public Map<String, FederatedNetworkOrder> retrieveActiveFederatedOrders() throws SubnetAddressesCapacityReachedException, InvalidCidrException;
}
