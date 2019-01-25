package org.fogbow.federatednetwork.datastore.orderstorage;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class AuditService {

    @Autowired
    private OrderStateChangeRepository orderStateChangeRepository;

    public void updateStateTimestamp(FederatedNetworkOrder order) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        OrderStateChange orderStateChange = new OrderStateChange(currentTimestamp, order, order.getOrderState());
        this.orderStateChangeRepository.save(orderStateChange);
    }

}
