package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbowcloud.manager.core.models.InstanceStatus;
import org.fogbowcloud.manager.core.models.ResourceType;
import org.fogbowcloud.manager.core.models.instances.Instance;
import org.fogbowcloud.manager.core.models.orders.Order;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Properties;

public class OrderController {

    Properties properties;

    public OrderController(Properties properties) {
        this.properties = properties;
    }

    public void setEmptyFieldsAndActivateOrder(FederatedOrder order, FederationUser federationUser) {
        throw new NotImplementedException();
    }


    public Order getOrder(String orderId) {
        throw new NotImplementedException();
    }

    public void deleteOrder(String orderId) {
        throw new NotImplementedException();
    }

    public Instance getResourceInstance(String orderId) {
        throw new NotImplementedException();
    }

    public List<InstanceStatus> getInstancesStatus(FederationUser federationUser, ResourceType resourceType) {
        throw new NotImplementedException();
    }

    private List<Order> getAllOrders(FederationUser federationUser, ResourceType resourceType) {
        throw new NotImplementedException();
    }

    public void updateOrderId(FederatedComputeOrder compute, String newId) {
        throw new NotImplementedException();
    }
}
