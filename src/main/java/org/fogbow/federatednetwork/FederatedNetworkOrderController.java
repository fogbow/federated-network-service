package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.ConfigurationConstants;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.model.*;
import org.fogbow.federatednetwork.utils.AgentCommunicatorUtil;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbow.federatednetwork.utils.PropertiesHolder;

import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {

    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);
    public static final String RAS_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_NAME);

    private FederatedNetworkOrdersHolder orderHolders;

    public FederatedNetworkOrderController() {
        this.orderHolders = FederatedNetworkOrdersHolder.getInstance();
    }

    // Federated Network methods

    public void activateFederatedNetwork(FederatedNetworkOrder federatedNetwork, FederationUserToken federationUser)
            throws InvalidCidrException {

        synchronized (federatedNetwork) {
            federatedNetwork.setUser(federationUser);

            SubnetUtils.SubnetInfo subnetInfo = null;
            subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidr());

            if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
                LOGGER.error(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
                throw new InvalidCidrException(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
            }
            if (AgentCommunicatorUtil.createFederatedNetwork(federatedNetwork.getCidr(), subnetInfo.getLowAddress())) {
                federatedNetwork.setOrderState(OrderState.FULFILLED);
            } else {
                federatedNetwork.setOrderState(OrderState.FAILED);
            }
            orderHolders.putOrder(federatedNetwork);
        }
    }

    public void deleteFederatedNetwork(String federatedNetworkId, FederationUserToken federationUserToken)
            throws NotEmptyFederatedNetworkException, FederatedNetworkNotFoundException, AgentCommucationException, UnauthorizedRequestException {
        LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, federationUserToken, federatedNetworkId));
        FederatedNetworkOrder federatedNetwork = this.getFederatedNetwork(federatedNetworkId, federationUserToken);

        synchronized (federatedNetwork) {
            if (federatedNetwork == null) {
                throw new IllegalArgumentException(
                        String.format(Messages.Exception.UNABLE_TO_FIND_FEDERATED_NETWORK, federatedNetworkId));
            }
            LOGGER.info(String.format(Messages.Info.DELETING_FEDERATED_NETWORK, federatedNetwork.toString()));
            if (!federatedNetwork.getComputeIdsAndIps().isEmpty()) {
                throw new NotEmptyFederatedNetworkException();
            }
            boolean wasDeleted = AgentCommunicatorUtil.deleteFederatedNetwork(federatedNetwork.getCidr());
            if (wasDeleted == true || federatedNetwork.getOrderState() == OrderState.FAILED) {
                // If the state of the order is FAILED, this is because in the creation, it was not possible to
                // connect to the Agent. Thus, there is nothing to remove at the Agent, and an exception does not
                // need to be thrown.
                LOGGER.info(String.format(Messages.Info.DELETED_FEDERATED_NETWORK, federatedNetwork.toString()));
                orderHolders.removeOrder(federatedNetworkId);
                federatedNetwork.setOrderState(OrderState.DEACTIVATED);
            } else {
                throw new AgentCommucationException();
            }
        }
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, FederationUserToken federationUserToken)
            throws FederatedNetworkNotFoundException, UnauthorizedRequestException {

        FederatedNetworkOrder federatedNetworkOrder = orderHolders.getOrder(federatedNetworkId);

        if (federatedNetworkOrder != null) {
            if (federatedNetworkOrder.getUser().equals(federationUserToken)) {
                return federatedNetworkOrder;
            }
            throw new UnauthorizedRequestException();
        }
        throw new FederatedNetworkNotFoundException(federatedNetworkId);
    }

    public Collection<InstanceStatus> getFederatedNetworksStatusByUser(FederationUserToken federationUserToken) {
        Collection<FederatedNetworkOrder> orders = this.orderHolders.getActiveOrdersMap().values();

        // Filter all orders of resourceType from federationUser that are not closed (closed orders have been deleted by
        // the user and should not be seen; they will disappear from the system).
        return orders.stream()
                    .filter(order -> order.getUser().equals(federationUserToken))
                    .filter(order -> !order.getOrderState().equals(OrderState.DEACTIVATED))
                    .map(orderToInstanceStatus())
                    .collect(Collectors.toList());
    }

    private Function<FederatedNetworkOrder, InstanceStatus> orderToInstanceStatus() {
        return order -> {
            InstanceState status = order.getInstanceStateFromOrderState();
            InstanceStatus instanceStatus = new InstanceStatus(order.getId(), order.getName(), RAS_NAME, status);
            return instanceStatus;
        };
    }
}
