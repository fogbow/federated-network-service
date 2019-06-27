package cloud.fogbow.fns.core.datastore;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.core.datastore.orderstorage.RecoveryService;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class DatabaseManager implements StableStorage {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;

    @Autowired
    private RecoveryService recoveryService;

    @Autowired
    private AuditService auditService;

    private DatabaseManager() {
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    @Override
    public void put(FederatedNetworkOrder order) throws UnexpectedException {
        recoveryService.put(order);
        auditService.updateStateTimestamp(order);
    }

    @Override
    public Map<String, FederatedNetworkOrder> retrieveActiveFederatedOrders() {
        return recoveryService.readActiveOrders();
    }

    public SynchronizedDoublyLinkedList<FederatedNetworkOrder> readActiveOrders(OrderState orderState) {
        SynchronizedDoublyLinkedList<FederatedNetworkOrder> synchronizedDoublyLinkedList = new SynchronizedDoublyLinkedList<>();

        for (FederatedNetworkOrder order : this.recoveryService.readActiveOrdersByState(orderState)) {
            synchronizedDoublyLinkedList.addItem(order);
        }
        return synchronizedDoublyLinkedList;
    }

    public void setRecoveryService(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

}
