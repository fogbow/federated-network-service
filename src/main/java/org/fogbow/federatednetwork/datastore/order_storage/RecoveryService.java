package org.fogbow.federatednetwork.datastore.order_storage;

import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecoveryService {

    @Autowired OrderRepository orderRepository;

    public RecoveryService() { }

    public FederatedOrder put(FederatedOrder order) {
        return orderRepository.save(order);
    }

    public List<FederatedOrder> readActiveOrders(OrderState orderState) {

        // If the state is closed, do a filter to not include orders with null instance id
        if (orderState == OrderState.CLOSED) {

            List<FederatedOrder> filteredOrdersList = new ArrayList<>();

            for (FederatedOrder order: orderRepository.findByOrderState(orderState)) {
                if (order.getInstanceId() != null) {
                    filteredOrdersList.add(order);
                }
            }

            return filteredOrdersList;
        }

        return orderRepository.findByOrderState(orderState);

    }
}
