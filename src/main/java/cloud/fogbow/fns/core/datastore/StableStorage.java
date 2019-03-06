package cloud.fogbow.fns.core.datastore;

import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;

import java.util.Map;

public interface StableStorage {

    /**
     * Add or update the federatedNetworkOrder into database, so it can be recovered in case of a crash.
     * @param order {@link FederatedNetworkOrder}
     */
    public void put(FederatedNetworkOrder order);

    /**
     * Retrieve all federated networks that have not been deactivated.
     * @return A map of order id and the corresponding {@link FederatedNetworkOrder}
     */
    public Map<String, FederatedNetworkOrder> retrieveActiveFederatedOrders() throws SubnetAddressesCapacityReachedException, InvalidCidrException;
}
