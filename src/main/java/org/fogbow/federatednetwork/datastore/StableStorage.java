package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.RedirectedComputeOrder;
import org.fogbowcloud.manager.core.models.linkedlists.SynchronizedDoublyLinkedList;
import org.fogbowcloud.manager.core.models.orders.OrderState;

public interface StableStorage {

    /**
     * Add the federatedNetworkOrder into database, so we can recovery it when necessary.
     *
     * @param federatedNetworkOrder {@link FederatedNetworkOrder}
     */
    public void addRedirectedCompute(FederatedNetworkOrder federatedNetworkOrder);

    /**
     * Update the federatedNetworkOrder when transition occurs.
     *
     * @param federatedNetworkOrder {@link FederatedNetworkOrder}
     */
    public void updateFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder);

    /**
     * Retrieve orders from the database based on its state.
     *
     * @param orderState {@link OrderState}
     * @return {@link SynchronizedDoublyLinkedList}
     */
    public SynchronizedDoublyLinkedList readActiveFederatedNetworkOrder(OrderState orderState);

    /**
     * Add the redirectedComputeOrder into database, so we can recovery it when necessary.
     *
     * @param redirectedComputeOrder {@link FederatedNetworkOrder}
     */
    public void addRedirectedCompute(RedirectedComputeOrder redirectedComputeOrder);

    /**
     * Update the redirectedComputeOrder when transition occurs.
     *
     * @param redirectedComputeOrder {@link FederatedNetworkOrder}
     */
    public void deleteRedirectedCompute(RedirectedComputeOrder redirectedComputeOrder);

    /**
     * Retrieve orders from the database based on its state.
     *
     * @param orderState {@link OrderState}
     * @return {@link SynchronizedDoublyLinkedList}
     */
    public SynchronizedDoublyLinkedList readActiveRedirectedComputeOrder(OrderState orderState);

}
