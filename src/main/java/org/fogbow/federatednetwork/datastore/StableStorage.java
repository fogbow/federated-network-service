package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbowcloud.manager.core.models.linkedlists.SynchronizedDoublyLinkedList;
import org.fogbowcloud.manager.core.models.orders.OrderState;

public interface StableStorage {

    /**
     * Add the federatedOrder into database, so we can recovery it when necessary.
     *
     * @param federatedOrder {@link FederatedOrder}
     */
    public void add(FederatedOrder federatedOrder);

    /**
     * Update the federatedOrder when transition occurs.
     *
     * @param federatedOrder {@link FederatedOrder}
     */
    public void update(FederatedOrder federatedOrder);

    /**
     * Retrieve orders from the database based on its state.
     *
     * @param orderState {@link OrderState}
     * @return {@link SynchronizedDoublyLinkedList}
     */
    public SynchronizedDoublyLinkedList readActiveOrders(OrderState orderState);
}
