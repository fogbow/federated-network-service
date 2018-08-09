package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederateComputeUtil;
import org.fogbow.federatednetwork.model.FederatedComputeInstance;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbow.federatednetwork.model.RedirectedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbowcloud.manager.core.models.InstanceStatus;
import org.fogbowcloud.manager.core.models.ResourceType;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.instances.Instance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.fogbow.federatednetwork.ConfigurationConstants.FEDERATED_NETWORK_AGENT_ADDRESS;
import static org.fogbow.federatednetwork.ConfigurationConstants.FEDERATED_NETWORK_AGENT_USER;
import static org.fogbow.federatednetwork.ConfigurationConstants.FEDERATED_NETWORK_PRE_SHARED_KEY;

public class OrderController {

    Properties properties;
    Map<String, FederatedNetworkOrder> activeFederatedNetworks;
    Map<String, RedirectedComputeOrder> activeRedirectedComputes;

    public OrderController(Properties properties) {
        this.properties = properties;
        this.activeFederatedNetworks = new ConcurrentHashMap<>();
        this.activeRedirectedComputes = new ConcurrentHashMap<>();
    }

    // Federated Network methods

    public void setEmptyFieldsAndActivateFederatedeNetwork(FederatedNetworkOrder order, FederationUser federationUser) {
        throw new NotImplementedException();
    }


    public FederatedNetworkOrder getFederatedNetwork(String orderId) {
        throw new NotImplementedException();
    }

    public void deleteFederatedNetwork(String orderId) {
        throw new NotImplementedException();
    }

    public Instance getResourceInstance(String orderId) {
        throw new NotImplementedException();
    }

    public List<InstanceStatus> getInstancesStatus(FederationUser federationUser, ResourceType resourceType) {
        throw new NotImplementedException();
    }

    private List<FederatedNetworkOrder> getAllFederatedNetworks(FederationUser federationUser, ResourceType resourceType) {
        Collection<FederatedNetworkOrder> orders = this.activeFederatedNetworks.values();

        // Filter all orders of resourceType from federationUser that are not closed (closed orders have been deleted by
        // the user and should not be seen; they will disappear from the system as soon as the closedProcessor thread
        // process them).
        List<FederatedNetworkOrder> requestedOrders =
                orders.stream()
                        .filter(order -> order.getFederationUser().equals(federationUser))
                        .filter(order -> !order.getOrderState().equals(OrderState.CLOSED))
                        .collect(Collectors.toList());
        return requestedOrders;
    }

    // Compute methods

    public RedirectedComputeOrder addFederationUserDataIfApplied(RedirectedComputeOrder redirectedComputeOrderOld) throws IOException {
        String federatedNetworkId = redirectedComputeOrderOld.getFederatedNetworkId();
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            String federatedIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetworkId);
            String cidr = FederatedNetworkUtil.getCidrFromNetworkId(federatedNetworkId);
            ComputeOrder incrementedComputeOrder = FederateComputeUtil.addUserData(redirectedComputeOrderOld.getComputeOrder(), federatedIp,
                    properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS), cidr, properties.getProperty(FEDERATED_NETWORK_PRE_SHARED_KEY));
            redirectedComputeOrderOld.setComputeOrder(incrementedComputeOrder);
        }
        return redirectedComputeOrderOld;
    }

    public void updateIdOnComputeCreation(RedirectedComputeOrder redirectedCompute, String newId) {
        ComputeOrder computeOrder = redirectedCompute.getComputeOrder();
        String federatedNetworkId = redirectedCompute.getFederatedNetworkId();
        // if compute is federated
        if (redirectedCompute != null && federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            // store compute into database
            redirectedCompute.updateIdOnComputeCreation(newId);
            activeRedirectedComputes.put(computeOrder.getId(), redirectedCompute);
        }
    }


    public ComputeInstance addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance,
                                                                   FederationUser federationUser) {
        RedirectedComputeOrder redirectedComputeOrder = activeRedirectedComputes.get(computeInstance.getId());
        if (redirectedComputeOrder != null) {
            String federatedIp = redirectedComputeOrder.getFederatedIp();
            FederatedComputeInstance federatedComputeInstance = new FederatedComputeInstance(computeInstance, federatedIp);
            return federatedComputeInstance;
        }
        return computeInstance;
    }

    public void deleteCompute(String computeId) {
        RedirectedComputeOrder redirectedComputeOrder = activeRedirectedComputes.get(computeId);
        String federatedIp = redirectedComputeOrder.getFederatedIp();
        String federatedNetworkId = redirectedComputeOrder.getFederatedNetworkId();
        FederatedNetworkOrder federatedNetworkOrder = activeFederatedNetworks.get(federatedNetworkId);
        federatedNetworkOrder.removeAssociatedIp(federatedIp);
    }
}
