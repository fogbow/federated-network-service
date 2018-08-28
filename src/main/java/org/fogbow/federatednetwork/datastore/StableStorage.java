package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;

import java.util.Map;
import java.util.Set;

public interface StableStorage {

    /**
     * Add or update the federatedNetworkOrder into database, so we can recovery it when necessary.
     * @param federatedOrder {@link FederatedOrder}
     */
    public void put(FederatedOrder federatedOrder);

    /**
     * Retrieve federated networks from the database based on user given.
     * @param user {@link FederationUserToken}
     * @return A set of federated networks {@link FederatedNetworkOrder}
     */
    public Set<FederatedOrder> readActiveFederatedNetworks(FederationUserToken user);

    /**
     * Retrieve all federated networks, may be interest to create a database recovery
     * @return A map of user id to federated order {@link FederatedOrder}
     */
    public Map<String, Set<FederatedOrder>> retrieveActiveFederatedNetworks();
}
