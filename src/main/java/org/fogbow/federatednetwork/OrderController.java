package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.ConfigurationPropertiesKeys;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.model.*;
import org.fogbow.federatednetwork.utils.AgentCommunicatorUtil;
import org.fogbow.federatednetwork.utils.FederateComputeUtil;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbowcloud.ras.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.ras.core.models.InstanceStatus;
import org.fogbowcloud.ras.core.models.instances.ComputeInstance;
import org.fogbowcloud.ras.core.models.instances.InstanceState;
import org.fogbowcloud.ras.core.models.orders.ComputeOrder;
import org.fogbowcloud.ras.core.models.orders.OrderState;
import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.fogbow.federatednetwork.constants.ConfigurationPropertiesKeys.FEDERATED_NETWORK_AGENT_ADDRESS;
import static org.fogbow.federatednetwork.constants.ConfigurationPropertiesKeys.FEDERATED_NETWORK_PRE_SHARED_KEY;

public class OrderController {

    private static final Logger LOGGER = Logger.getLogger(OrderController.class);

    private Properties properties;
    private SharedOrderHolders orderHolders;

    public OrderController(Properties properties) throws SubnetAddressesCapacityReachedException, InvalidCidrException,
            SQLException {
        this.properties = properties;
        this.orderHolders = SharedOrderHolders.getInstance();
    }

    // Federated Network methods

    public String activateFederatedNetwork(FederatedNetworkOrder federatedNetwork, FederationUserToken federationUser)
            throws InvalidCidrException, AgentCommucationException, SQLException {
        FederatedUser user = new FederatedUser(federationUser.getUserId(), federationUser.getUserName());
        federatedNetwork.setUser(user);

        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidrNotation());

        if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
            LOGGER.error(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidrNotation()));
            throw new InvalidCidrException(String.format(Messages.Exception.INVALID_CIDR,
                    federatedNetwork.getCidrNotation()));
        }

        boolean createdSuccessfully = AgentCommunicatorUtil.createFederatedNetwork(federatedNetwork.getCidrNotation(),
                subnetInfo.getLowAddress(), properties);
        if (createdSuccessfully) {
            federatedNetwork.setCachedInstanceState(InstanceState.READY);
            federatedNetwork.setOrderState(OrderState.FULFILLED);
            orderHolders.putOrder(federatedNetwork);
            return federatedNetwork.getId();
        }
        throw new AgentCommucationException();
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, FederatedUser user)
            throws FederatedNetworkNotFoundException, UnauthenticatedUserException {

        FederatedOrder federatedOrder = orderHolders.getOrder(federatedNetworkId);
        FederatedNetworkOrder federatedNetworkOrder = (FederatedNetworkOrder) federatedOrder;
        if (federatedNetworkOrder != null) {
            if (federatedNetworkOrder.getUser().equals(user)) {
                return federatedNetworkOrder;
            }
            throw new UnauthenticatedUserException();
        }
        throw new FederatedNetworkNotFoundException(federatedNetworkId);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, FederatedUser user)
            throws NotEmptyFederatedNetworkException, FederatedNetworkNotFoundException, AgentCommucationException,
            UnauthenticatedUserException, SQLException {
        LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, user, federatedNetworkId));
        FederatedNetworkOrder federatedNetwork = this.getFederatedNetwork(federatedNetworkId, user);
        if (federatedNetwork == null) {
            throw new IllegalArgumentException(
                    String.format(Messages.Exception.UNABLE_TO_FIND_FEDERATED_NETWORK, federatedNetworkId));
        }
        LOGGER.info(String.format(Messages.Info.DELETING_FEDERATED_NETWORK, federatedNetwork.toString()));
        if (!federatedNetwork.getComputesIp().isEmpty()) {
            throw new NotEmptyFederatedNetworkException();
        }
        boolean wasDeleted = AgentCommunicatorUtil.deleteFederatedNetwork(federatedNetwork.getCidrNotation(), properties);
        if (wasDeleted == true) {
            LOGGER.info(String.format(Messages.Info.DELETED_FEDERATED_NETWORK, federatedNetwork.toString()));
            orderHolders.removeOrder(federatedNetworkId);
            federatedNetwork.setOrderState(OrderState.DEACTIVATED);
        } else {
            throw new AgentCommucationException();
        }
    }

    public Collection<InstanceStatus> getUserFederatedNetworksStatus(FederatedUser user) {
        Collection<FederatedOrder> orders = this.orderHolders.getActiveOrdersMap().values();

        // Filter all orders of resourceType from federationUser that are not closed (closed orders have been deleted by
        // the user and should not be seen; they will disappear from the system).
        List<FederatedNetworkOrder> requestedOrders =
                orders.stream()
                        .filter(order -> order instanceof FederatedNetworkOrder)
                        .map(order -> (FederatedNetworkOrder) order)
                        .filter(order -> order.getUser().equals(user))
                        .filter(order -> !order.getOrderState().equals(OrderState.CLOSED))
                        .collect(Collectors.toList());
        return getFederatedNetworksStatus(requestedOrders);
    }

    private Collection<InstanceStatus> getFederatedNetworksStatus(Collection<FederatedNetworkOrder> allFederatedNetworks) {
        Collection<InstanceStatus> instanceStatusList = new ArrayList<>();
        String memberName = properties.getProperty(ConfigurationPropertiesKeys.RAS_NAME);
        Iterator<FederatedNetworkOrder> iterator = allFederatedNetworks.iterator();
        while (iterator.hasNext()) {
            FederatedNetworkOrder federatedNetwork = iterator.next();
            InstanceStatus instanceStatus = new InstanceStatus(federatedNetwork.getId(), memberName,
                    federatedNetwork.getCachedInstanceState());
            instanceStatusList.add(instanceStatus);
        }
        return new ArrayList<>(instanceStatusList);
    }

    // Compute methods

    public ComputeOrder addFederationUserTokenDataIfApplied(FederatedComputeOrder federatedComputeOrder,
                                                            FederationUserToken federationUser) throws
            IOException, SubnetAddressesCapacityReachedException, FederatedNetworkNotFoundException,
            InvalidCidrException, SQLException {
        FederatedUser user = new FederatedUser(federationUser.getUserId(), federationUser.getUserName());
        federatedComputeOrder.setUser(user);
        String federatedNetworkId = federatedComputeOrder.getFederatedNetworkId();

        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = orderHolders.getFederatedNetwork(federatedNetworkId);
            if (federatedNetworkOrder == null) {
                throw new FederatedNetworkNotFoundException(federatedNetworkId);
            }
            String federatedIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetworkOrder);
            federatedComputeOrder.setFederatedIp(federatedIp);
            String cidr = federatedNetworkOrder.getCidrNotation();
            ComputeOrder incrementedComputeOrder = FederateComputeUtil.
                    addUserData(federatedComputeOrder.getComputeOrder(), federatedIp,
                    properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS), cidr,
                            properties.getProperty(FEDERATED_NETWORK_PRE_SHARED_KEY));
            federatedComputeOrder.setComputeOrder(incrementedComputeOrder);
        }
        return federatedComputeOrder.getComputeOrder();
    }

    public void updateIdOnComputeCreation(FederatedComputeOrder federatedCompute, String newId) throws SQLException {
        String federatedNetworkId = federatedCompute.getFederatedNetworkId();
        // if compute is federated
        if (federatedCompute != null && federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            // store compute into database
            federatedCompute.updateIdOnComputeCreation(newId);
            orderHolders.putOrder(federatedCompute);
        }
    }

    public ComputeInstance addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance,
                                                                FederatedUser user)
            throws UnauthenticatedUserException {
        FederatedComputeOrder federatedComputeOrder = orderHolders.getFederatedCompute(computeInstance.getId());
        if (federatedComputeOrder != null) {
            FederatedUser computeUser = federatedComputeOrder.getUser();
            if (computeUser.equals(user)) {
                String federatedIp = federatedComputeOrder.getFederatedIp();
                FederatedComputeInstance federatedComputeInstance = new FederatedComputeInstance(computeInstance,
                        federatedIp);
                return federatedComputeInstance;
            } else {
                throw new UnauthenticatedUserException();
            }
        }
        return computeInstance;
    }

    public void deleteCompute(String computeId, FederatedUser user) throws FederatedNetworkNotFoundException,
            UnauthenticatedUserException, SQLException {
        FederatedComputeOrder federatedComputeOrder = orderHolders.getFederatedCompute(computeId);
        if (federatedComputeOrder != null) {
            if (!federatedComputeOrder.getUser().equals(user)) {
                throw new UnauthenticatedUserException();
            }
            String federatedIp = federatedComputeOrder.getFederatedIp();
            String federatedNetworkId = federatedComputeOrder.getFederatedNetworkId();
            FederatedNetworkOrder federatedNetworkOrder = orderHolders.getFederatedNetwork(federatedNetworkId);
            if (federatedNetworkOrder == null) {
                throw new FederatedNetworkNotFoundException(federatedNetworkId);
            }
            federatedNetworkOrder.removeAssociatedIp(federatedIp);
            federatedComputeOrder.deactivateCompute();
            orderHolders.removeOrder(computeId);
        }
    }

    public void rollbackInFailedPost(FederatedComputeOrder federatedCompute) throws SQLException {
        FederatedNetworkOrder federatedNetwork = orderHolders.getFederatedNetwork(federatedCompute.getFederatedNetworkId());
        federatedNetwork.removeAssociatedIp(federatedCompute.getFederatedIp());
    }
}
