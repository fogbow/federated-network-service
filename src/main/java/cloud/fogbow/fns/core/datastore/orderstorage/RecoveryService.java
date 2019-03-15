package cloud.fogbow.fns.core.datastore.orderstorage;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.ras.core.datastore.services.FogbowDatabaseService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecoveryService extends FogbowDatabaseService<FederatedNetworkOrder> {

    @Autowired
    private OrderRepository orderRepository;

    private static final Logger LOGGER = Logger.getLogger(RecoveryService.class);

    public void put(FederatedNetworkOrder order) throws UnexpectedException {
        safeSave(order, this.orderRepository);
    }

    public List<FederatedNetworkOrder> readActiveOrdersByState(OrderState orderState) {
        return orderRepository.findByOrderState(orderState);
    }

    public Map<String, FederatedNetworkOrder> readActiveOrders() {
        Map<String, FederatedNetworkOrder> activeOrdersMap = new ConcurrentHashMap<>();
        for (FederatedNetworkOrder order: orderRepository.findAll()) {
            if (!(order.getOrderState().equals(OrderState.DEACTIVATED))) {
                try {
                    FederatedNetworkUtil.fillCacheOfFreeIps(order);
                } catch (SubnetAddressesCapacityReachedException e) {
                    LOGGER.info(Messages.Exception.NO_MORE_IPS_AVAILABLE);
                } catch (InvalidCidrException e) {
                    LOGGER.error(Messages.Error.INVALID_CIDR);
                }
                activeOrdersMap.put(order.getId(), order);
            }
        }
        return activeOrdersMap;
    }
}
