package cloud.fogbow.fns.datastore.orderstorage;

import cloud.fogbow.fns.model.FederatedNetworkOrder;
import cloud.fogbow.fns.model.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface OrderRepository extends JpaRepository<FederatedNetworkOrder, String> {

    List<FederatedNetworkOrder> findByOrderState(OrderState orderState);
}
