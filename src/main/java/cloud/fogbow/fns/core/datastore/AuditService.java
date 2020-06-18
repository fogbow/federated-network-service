package cloud.fogbow.fns.core.datastore;

import cloud.fogbow.common.datastore.FogbowDatabaseService;
import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.fns.core.datastore.orderstorage.AuditableOrderStateChange;
import cloud.fogbow.fns.core.datastore.orderstorage.OrderStateChangeRepository;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class AuditService extends FogbowDatabaseService<AuditableOrderStateChange> {

    @Autowired
    private OrderStateChangeRepository orderStateChangeRepository;

    public void updateStateTimestamp(FederatedNetworkOrder order) throws InternalServerErrorException {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        AuditableOrderStateChange orderStateChange = new AuditableOrderStateChange(currentTimestamp, order, order.getOrderState());
        safeSave(orderStateChange, this.orderStateChangeRepository);
    }

}
