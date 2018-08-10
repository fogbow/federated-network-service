package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.controllers.FederateComputeUtil;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeInstance;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.RedirectedComputeOrder;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbowcloud.manager.core.models.InstanceStatus;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.instances.InstanceState;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.fogbow.federatednetwork.ConfigurationConstants.FEDERATED_NETWORK_AGENT_ADDRESS;
import static org.fogbow.federatednetwork.ConfigurationConstants.FEDERATED_NETWORK_PRE_SHARED_KEY;

public class OrderController {

    private static final Logger LOGGER = Logger.getLogger(OrderController.class);

    private Properties properties;
    private Map<String, FederatedNetworkOrder> activeFederatedNetworks;
    private Map<String, RedirectedComputeOrder> activeRedirectedComputes;

    public OrderController(Properties properties) {
        this.properties = properties;
        this.activeFederatedNetworks = new ConcurrentHashMap<>();
        this.activeRedirectedComputes = new ConcurrentHashMap<>();
    }

    // Federated Network methods

    public String activateFederatedNetwork(FederatedNetworkOrder federatedNetwork, FederationUser federationUser) {
        initFederatedNetwork(federatedNetwork);

        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidrNotation());

        if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
            LOGGER.error("Subnet (" + federatedNetwork.getCidrNotation() + ") invalid");
            // TODO: we should throw an exception here
            return "";
        }

        boolean createdSuccessfully = AgentCommunicator.createFederatedNetwork(federatedNetwork.getCidrNotation(), subnetInfo.getLowAddress(), properties);
        if (createdSuccessfully) {
            federatedNetwork.setCachedInstanceState(InstanceState.READY);
            federatedNetwork.setOrderState(OrderState.FULFILLED);
            activeFederatedNetworks.put(federatedNetwork.getId(), federatedNetwork);
            return federatedNetwork.getId();
        }
        // TODO: we should throw an exception here
        return "";
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, FederationUser user) {
        // TODO: filter the user
        return activeFederatedNetworks.get(federatedNetworkId);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, FederationUser user) throws NotEmptyFederatedNetworkException {
        LOGGER.info("Initializing delete method, user: " + user + ", federated network id: " + federatedNetworkId);
        FederatedNetworkOrder federatedNetwork = this.getFederatedNetwork(federatedNetworkId, user);
        if (federatedNetwork == null) {
            throw new IllegalArgumentException(
                    FederatedNetworkConstants.NOT_FOUND_FEDERATED_NETWORK_MESSAGE
                            + federatedNetworkId);
        }
        LOGGER.info("Trying to delete federated network: " + federatedNetwork.toString());
        if (!federatedNetwork.getComputesIp().isEmpty()) {
            throw new NotEmptyFederatedNetworkException();
        }
        boolean wasDeleted = AgentCommunicator.deleteFederatedNetwork(federatedNetwork.getCidrNotation(), properties);
        if (wasDeleted == true) {
            LOGGER.info("Successfully deleted federated network: " + federatedNetwork.toString() + " on agent.");
            activeFederatedNetworks.remove(federatedNetworkId);
            federatedNetwork.setOrderState(OrderState.DEACTIVATED);
        }
        // TODO: We should throw an exception here.
    }

    public Collection<InstanceStatus> getUserFederatedNetworksStatus(FederationUser user) {
        Collection<FederatedNetworkOrder> allFederatedNetworks = activeFederatedNetworks.values();
        Collection<InstanceStatus> allFederatedNetworksStatus = getFederatedNetworksStatus(allFederatedNetworks);
        // TODO: filter by user
        return allFederatedNetworksStatus;
    }

    private void initFederatedNetwork(FederatedNetworkOrder federatedNetwork) {
        federatedNetwork.setId(String.valueOf(UUID.randomUUID()));
        federatedNetwork.setIpsServed(1);
        federatedNetwork.setFreedIps(new LinkedList<>());
    }

    private Collection<InstanceStatus> getFederatedNetworksStatus(Collection<FederatedNetworkOrder> allFederatedNetworks) {
        Collection<InstanceStatus> instanceStatusList = new ArrayList<>();
        Iterator<FederatedNetworkOrder> iterator = allFederatedNetworks.iterator();
        while (iterator.hasNext()) {
            // TODO: Give a provider for fednets, for now we don't have this information in FederatedNetwork class.
            FederatedNetworkOrder federatedNetwork = iterator.next();
            InstanceStatus instanceStatus = new InstanceStatus(federatedNetwork.getId(), "", federatedNetwork.getCachedInstanceState());
            instanceStatusList.add(instanceStatus);
        }
        return new ArrayList<InstanceStatus>(instanceStatusList);
    }

    private List<FederatedNetworkOrder> getAllFederatedNetworks(FederationUser federationUser) {
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

    public ComputeOrder addFederationUserDataIfApplied(RedirectedComputeOrder redirectedComputeOrderOld, FederationUser federationUser) throws
            IOException, SubnetAddressesCapacityReachedException {
        String federatedNetworkId = redirectedComputeOrderOld.getFederatedNetworkId();
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = activeFederatedNetworks.get(federatedNetworkId);
            String federatedIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetworkOrder);
            String cidr = federatedNetworkOrder.getCidrNotation();
            ComputeOrder incrementedComputeOrder = FederateComputeUtil.addUserData(redirectedComputeOrderOld.getComputeOrder(), federatedIp,
                    properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS), cidr, properties.getProperty(FEDERATED_NETWORK_PRE_SHARED_KEY));
            redirectedComputeOrderOld.setComputeOrder(incrementedComputeOrder);
        }
        return redirectedComputeOrderOld.getComputeOrder();
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
