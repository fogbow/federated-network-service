package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.utils.AgentCommunicatorUtil;
import org.fogbow.federatednetwork.utils.FederateComputeUtil;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeInstance;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
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
    private Map<String, FederatedComputeOrder> activeFederatedComputes;

    public OrderController(Properties properties) {
        this.properties = properties;
        this.activeFederatedNetworks = new ConcurrentHashMap<>();
        this.activeFederatedComputes = new ConcurrentHashMap<>();
    }

    // Federated Network methods

    public String activateFederatedNetwork(FederatedNetworkOrder federatedNetwork, FederationUser federationUser) throws InvalidCidrException {
        federatedNetwork.setFederationUser(federationUser);

        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidrNotation());

        if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
            LOGGER.error("Subnet (" + federatedNetwork.getCidrNotation() + ") invalid");
            // TODO: we should throw an exception here
            return "";
        }

        boolean createdSuccessfully = AgentCommunicatorUtil.createFederatedNetwork(federatedNetwork.getCidrNotation(), subnetInfo.getLowAddress(), properties);
        if (createdSuccessfully) {
            federatedNetwork.setCachedInstanceState(InstanceState.READY);
            federatedNetwork.setOrderState(OrderState.FULFILLED);
            activeFederatedNetworks.put(federatedNetwork.getId(), federatedNetwork);
            return federatedNetwork.getId();
        }
        // TODO: we should throw an exception here
        return "";
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, FederationUser user)
            throws FederatedNetworkNotFoundException {
        // TODO: filter the user
        FederatedNetworkOrder federatedNetworkOrder = activeFederatedNetworks.get(federatedNetworkId);
        if (federatedNetworkOrder != null && federatedNetworkOrder.getFederationUser().equals(user)) {
            return federatedNetworkOrder;
        }
        throw new FederatedNetworkNotFoundException(federatedNetworkId);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, FederationUser user)
            throws NotEmptyFederatedNetworkException, FederatedNetworkNotFoundException {
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
        boolean wasDeleted = AgentCommunicatorUtil.deleteFederatedNetwork(federatedNetwork.getCidrNotation(), properties);
        if (wasDeleted == true) {
            LOGGER.info("Successfully deleted federated network: " + federatedNetwork.toString() + " on agent.");
            activeFederatedNetworks.remove(federatedNetworkId);
            federatedNetwork.setOrderState(OrderState.DEACTIVATED);
        }
        // TODO: We should throw an exception here.
    }

    public Collection<InstanceStatus> getUserFederatedNetworksStatus(FederationUser user) {
        Collection<FederatedNetworkOrder> orders = this.activeFederatedNetworks.values();

        // Filter all orders of resourceType from federationUser that are not closed (closed orders have been deleted by
        // the user and should not be seen; they will disappear from the system).
        List<FederatedNetworkOrder> requestedOrders =
                orders.stream()
                        .filter(order -> order.getFederationUser().equals(user))
                        .filter(order -> !order.getOrderState().equals(OrderState.CLOSED))
                        .collect(Collectors.toList());
        return getFederatedNetworksStatus(requestedOrders);
    }

    private Collection<InstanceStatus> getFederatedNetworksStatus(Collection<FederatedNetworkOrder> allFederatedNetworks) {
        Collection<InstanceStatus> instanceStatusList = new ArrayList<>();
        Iterator<FederatedNetworkOrder> iterator = allFederatedNetworks.iterator();
        while (iterator.hasNext()) {
            // TODO: Give a provider for fednets, for now we don't have this information in FederatedNetwork class.
            FederatedNetworkOrder federatedNetwork = iterator.next();
            InstanceStatus instanceStatus = new InstanceStatus(federatedNetwork.getId(), "-", federatedNetwork.getCachedInstanceState());
            instanceStatusList.add(instanceStatus);
        }
        return new ArrayList<>(instanceStatusList);
    }

    // Compute methods

    public ComputeOrder addFederationUserDataIfApplied(FederatedComputeOrder federatedComputeOrder, FederationUser user) throws
            IOException, SubnetAddressesCapacityReachedException, FederatedNetworkNotFoundException, InvalidCidrException {
        federatedComputeOrder.getComputeOrder().setFederationUser(user);
        String federatedNetworkId = federatedComputeOrder.getFederatedNetworkId();

        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = activeFederatedNetworks.get(federatedNetworkId);
            if (federatedNetworkOrder == null) {
                throw new FederatedNetworkNotFoundException(federatedNetworkId);
            }
            String federatedIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetworkOrder);
            federatedComputeOrder.setFederatedIp(federatedIp);
            String cidr = federatedNetworkOrder.getCidrNotation();
            ComputeOrder incrementedComputeOrder = FederateComputeUtil.addUserData(federatedComputeOrder.getComputeOrder(), federatedIp,
                    properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS), cidr, properties.getProperty(FEDERATED_NETWORK_PRE_SHARED_KEY));
            federatedComputeOrder.setComputeOrder(incrementedComputeOrder);
        }
        return federatedComputeOrder.getComputeOrder();
    }

    public void updateIdOnComputeCreation(FederatedComputeOrder federatedCompute, String newId) {
        ComputeOrder computeOrder = federatedCompute.getComputeOrder();
        String federatedNetworkId = federatedCompute.getFederatedNetworkId();
        // if compute is federated
        if (federatedCompute != null && federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            // store compute into database
            federatedCompute.updateIdOnComputeCreation(newId);
            activeFederatedComputes.put(computeOrder.getId(), federatedCompute);
        }
    }


    public ComputeInstance addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance,
                                                                FederationUser federationUser) {
        FederatedComputeOrder federatedComputeOrder = activeFederatedComputes.get(computeInstance.getId());
        if (federatedComputeOrder != null) {
            String federatedIp = federatedComputeOrder.getFederatedIp();
            FederatedComputeInstance federatedComputeInstance = new FederatedComputeInstance(computeInstance, federatedIp);
            return federatedComputeInstance;
        }
        return computeInstance;
    }

    public void deleteCompute(String computeId) {
        FederatedComputeOrder federatedComputeOrder = activeFederatedComputes.get(computeId);
        if (federatedComputeOrder != null) {
            String federatedIp = federatedComputeOrder.getFederatedIp();
            String federatedNetworkId = federatedComputeOrder.getFederatedNetworkId();
            FederatedNetworkOrder federatedNetworkOrder = activeFederatedNetworks.get(federatedNetworkId);
            federatedNetworkOrder.removeAssociatedIp(federatedIp);
            federatedComputeOrder.deactivateCompute();
        }
    }

    public void rollbackInFailedPost(FederatedComputeOrder federatedCompute) {
        FederatedNetworkOrder federatedNetwork = activeFederatedNetworks.get(federatedCompute.getFederatedNetworkId());
        federatedNetwork.removeAssociatedIp(federatedCompute.getFederatedIp());
    }
}
