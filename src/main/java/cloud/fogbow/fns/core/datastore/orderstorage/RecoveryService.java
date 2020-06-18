package cloud.fogbow.fns.core.datastore.orderstorage;

import cloud.fogbow.common.datastore.FogbowDatabaseService;
import cloud.fogbow.common.exceptions.InvalidParameterException;
import cloud.fogbow.common.exceptions.UnacceptableOperationException;
import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.ComputeIdToFederatedNetworkIdMapping;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecoveryService extends FogbowDatabaseService<FederatedNetworkOrder> {

    @Autowired
    private OrderRepository orderRepository;

    private static final Logger LOGGER = Logger.getLogger(RecoveryService.class);

    public RecoveryService() {}

    public void put(FederatedNetworkOrder order) throws InternalServerErrorException {
        order.serializeSystemUser();
        safeSave(order, this.orderRepository);
    }

    public List<FederatedNetworkOrder> readActiveOrdersByState(OrderState orderState) {
        List<FederatedNetworkOrder> orders = orderRepository.findByOrderState(orderState);
        for (FederatedNetworkOrder order: orders) {
            if (!(order.getOrderState().equals(OrderState.DEACTIVATED))) {
                try {
                    ComputeIdToFederatedNetworkIdMapping mapper = ComputeIdToFederatedNetworkIdMapping.getInstance();
                    for (AssignedIp ip : order.getAssignedIps()) {
                        mapper.put(ip.getComputeId(), order.getId());
                    }
                    order.fillCacheOfFreeIps();
                } catch (UnacceptableOperationException e) {
                    LOGGER.info(Messages.Log.NO_MORE_IPS_AVAILABLE);
                } catch (InvalidParameterException e) {
                    LOGGER.error(Messages.Log.INVALID_CIDR);
                }
            }
        }
        return orders;
    }

    protected void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
