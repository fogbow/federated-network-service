package cloud.fogbow.fns.core.datastore.orderstorage;

import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecoveryService {

    @Autowired
    private OrderRepository orderRepository;

    public FederatedNetworkOrder put(FederatedNetworkOrder order) {
        return orderRepository.save(order);
    }

    public List<FederatedNetworkOrder> readActiveOrdersByState(OrderState orderState) {
        return orderRepository.findByOrderState(orderState);
    }

    public Map<String, FederatedNetworkOrder> readActiveOrders() {
        Map<String, FederatedNetworkOrder> activeOrdersMap = new ConcurrentHashMap<>();
        for (FederatedNetworkOrder order: orderRepository.findAll()) {
            try {
                FederatedNetworkUtil.fillCacheOfFreeIps((FederatedNetworkOrder) order);
            } catch (SubnetAddressesCapacityReachedException e) {
                // TODO put logging
            } catch (InvalidCidrException e) {
            }
            activeOrdersMap.put(order.getId(), order);
        }
        return activeOrdersMap;
    }
}
