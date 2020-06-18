package cloud.fogbow.fns.core.datastore;

import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;

public interface StableStorage {

    /**
     * Add or update the federatedNetworkOrder into database, so it can be recovered in case of a crash.
     * @param order {@link FederatedNetworkOrder}
     */
    public void put(FederatedNetworkOrder order) throws InternalServerErrorException;

    /**
     * Retrieve all federated networks whose state is orderState.
     * @return A list of {@link FederatedNetworkOrder}
     */
    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> readActiveOrders(OrderState orderState) throws InternalServerErrorException;
}
