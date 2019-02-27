package cloud.fogbow.fns.core.datastore.orderstorage;

import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class AuditService {

    @Autowired
    private OrderStateChangeRepository orderStateChangeRepository;

    public void updateStateTimestamp(FederatedNetworkOrder order) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        AuditableOrderStateChange orderStateChange = new AuditableOrderStateChange(currentTimestamp, order, order.getOrderState());
        this.orderStateChangeRepository.save(orderStateChange);
    }

}
