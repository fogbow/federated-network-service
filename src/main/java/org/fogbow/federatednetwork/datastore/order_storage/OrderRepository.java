package org.fogbow.federatednetwork.datastore.order_storage;

import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<FederatedOrder, String> {

    List<FederatedOrder> findByOrderState(OrderState orderState);
}
