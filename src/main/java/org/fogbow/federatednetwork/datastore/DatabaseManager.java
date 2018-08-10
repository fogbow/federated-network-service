package org.fogbow.federatednetwork.datastore;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.RedirectedComputeOrder;
import org.fogbowcloud.manager.core.exceptions.FatalErrorException;
import org.fogbowcloud.manager.core.models.linkedlists.SynchronizedDoublyLinkedList;
import org.fogbowcloud.manager.core.models.orders.OrderState;

import java.sql.SQLException;

public class DatabaseManager implements StableStorage {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class);
    private static final String ERROR_MASSAGE = "Error instantiating database manager";

    private static DatabaseManager instance;

    private DatabaseManager() throws SQLException {
        // instantiate orders storage
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            try {
                instance = new DatabaseManager();
            } catch (SQLException e) {
                LOGGER.error(ERROR_MASSAGE, e);
                throw new FatalErrorException(ERROR_MASSAGE, e);
            }
        }
        return instance;
    }

    @Override
    public void addRedirectedCompute(FederatedNetworkOrder federatedNetworkOrder) {

    }

    @Override
    public void updateFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder) {

    }

    @Override
    public SynchronizedDoublyLinkedList readActiveFederatedNetworkOrder(OrderState orderState) {
        return null;
    }

    @Override
    public void addRedirectedCompute(RedirectedComputeOrder redirectedComputeOrder) {

    }

    @Override
    public void deleteRedirectedCompute(RedirectedComputeOrder redirectedComputeOrder) {

    }

    @Override
    public SynchronizedDoublyLinkedList readActiveRedirectedComputeOrder(OrderState orderState) {
        return null;
    }
}
