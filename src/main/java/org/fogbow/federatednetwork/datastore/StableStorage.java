package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;

import java.util.Map;
import java.util.Set;

public interface StableStorage {

    /**
     * Add or update the federatedNetworkOrder into database, so we can recovery it when necessary.
     *
     * @param federatedNetworkOrder {@link FederatedNetworkOrder}
     */
    public void putFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder, FederationUserToken user);

    /**
     * Delete the federatedNetworkOrder.
     *
     * @param federatedNetworkOrder {@link FederatedNetworkOrder}
     */
    public void deleteFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder, FederationUserToken user);

    /**
     * Retrieve federated networks from the database based on user given.
     * @param user {@link FederationUserToken}
     * @return A set of federated networks {@link FederatedNetworkOrder}
     */
    public Set<FederatedNetworkOrder> readActiveFederatedNetworks(FederationUserToken user);

    /**
     * Retrieve all federated networks, may be interest to create a database recovery
     * @return A map of user id to federated networks {@link FederatedNetworkOrder}
     */
    public Map<String, Set<FederatedNetworkOrder>> retrieveActiveFederatedNetworks();

    /**
     * Add or update the federatedComputeOrder into database, so we can recovery it when necessary.
     *
     * @param federatedComputeOrder {@link FederatedNetworkOrder}
     */
    public void putFederatedCompute(FederatedComputeOrder federatedComputeOrder, FederationUserToken user);

    /**
     * Delete the federatedComputeOrder.
     *
     * @param federatedComputeOrder {@link FederatedNetworkOrder}
     */
    public void deleteFederatedCompute(FederatedComputeOrder federatedComputeOrder, FederationUserToken user);

    /**
     * Retrieve federated computes from the database based on user given.
     * @param user {@link FederationUserToken}
     * @return A set of federated computes {@link FederatedComputeOrder}
     */
    public Set<FederatedComputeOrder> readActiveFederatedComputes(FederationUserToken user);

    /**
     * Retrieve all federated computes, may be interest to create a database recovery
     * @return A map of user id to to federated computes {@link FederatedComputeOrder}
     */
    public Map<String, Set<FederatedComputeOrder>> retrieveActiveFederatedComputes();
}
