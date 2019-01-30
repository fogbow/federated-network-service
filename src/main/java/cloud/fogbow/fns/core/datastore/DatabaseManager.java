package cloud.fogbow.fns.core.datastore;

import cloud.fogbow.fns.core.datastore.orderstorage.RecoveryService;
import org.apache.log4j.Logger;
import cloud.fogbow.fns.core.datastore.orderstorage.AuditService;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
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
    public void put(FederatedNetworkOrder order) {
        recoveryService.put(order);
        auditService.updateStateTimestamp(order);
    }

    @Override
    public Map<String, FederatedNetworkOrder> retrieveActiveFederatedOrders() {
        return recoveryService.readActiveOrders();
    }

    public void setRecoveryService(RecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

}