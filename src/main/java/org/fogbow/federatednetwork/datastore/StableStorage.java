package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.util.Set;

public interface StableStorage {

    /**
     * Add or update the federatedNetworkOrder into database, so we can recovery it when necessary.
     *
     * @param federatedNetworkOrder {@link FederatedNetworkOrder}
     */
    public void putFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder, FederationUser user);

    /**
     * Delete the federatedNetworkOrder.
     *
     * @param federatedNetworkOrder {@link FederatedNetworkOrder}
     */
    public void deleteFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder, FederationUser user);

    /**
     * Retrieve federated networks from the database based on user given.
     * @param user {@link FederationUser}
     * @return A set of federated networks {@link FederatedNetworkOrder}
     */
    public Set<FederatedNetworkOrder> readActiveFederatedNetworks(FederationUser user);

    /**
     * Add or update the federatedComputeOrder into database, so we can recovery it when necessary.
     *
     * @param federatedComputeOrder {@link FederatedNetworkOrder}
     */
    public void putFederatedCompute(FederatedComputeOrder federatedComputeOrder, FederationUser user);

    /**
     * Delete the federatedComputeOrder.
     *
     * @param federatedComputeOrder {@link FederatedNetworkOrder}
     */
    public void deleteFederatedCompute(FederatedComputeOrder federatedComputeOrder, FederationUser user);

    /**
     * Retrieve federated computes from the database based on user given.
     * @param user {@link FederationUser}
     * @return A set of redirected compute {@link FederatedComputeOrder}
     */
    public Set<FederatedComputeOrder> readActiveFederatedComputes(FederationUser user);

}
