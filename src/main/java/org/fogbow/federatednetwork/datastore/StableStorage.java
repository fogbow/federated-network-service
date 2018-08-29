package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbowcloud.manager.core.models.linkedlists.SynchronizedDoublyLinkedList;
import org.fogbowcloud.manager.core.models.orders.OrderState;
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
     * Retrieve federated networks from the database for a given state.
     * @param orderState {@link OrderState}
     * @return A list of orders in a specific state
     */
    public SynchronizedDoublyLinkedList readActiveFederatedNetworks(OrderState orderState);

    /**
     * Retrieve all federated networks, may be interest to create a database recovery
     * @return A map of user id to federated order {@link FederatedOrder}
     */
    public Map<String, FederatedOrder> retrieveActiveFederatedNetworks();
}
