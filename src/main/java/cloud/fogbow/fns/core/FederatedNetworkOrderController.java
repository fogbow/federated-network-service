package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.core.exceptions.AgentCommucationException;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.InstanceState;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {
    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);
    public static final String RAS_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.LOCAL_MEMBER_ID_KEY);

    // Federated Network methods

    public void addFederatedNetwork(FederatedNetworkOrder federatedNetwork, SystemUser systemUser)
            throws InvalidCidrException {
        synchronized (federatedNetwork) {
            federatedNetwork.setSystemUser(systemUser);

            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidr());

            if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
                LOGGER.error(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
                throw new InvalidCidrException(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
            }
            // TODO ARNETT REMOVE THIS
//            if (AgentCommunicatorUtil.createFederatedNetwork(federatedNetwork.getCidr(), subnetInfo.getLowAddress())) {
//                federatedNetwork.setOrderState(OrderState.FULFILLED);
//            } else {
//                federatedNetwork.setOrderState(OrderState.FAILED);
//            }

            OrderStateTransitioner.activateOrder(federatedNetwork);
        }
    }

    public void deleteFederatedNetwork(String federatedNetworkId, SystemUser systemUser)
            throws NotEmptyFederatedNetworkException, FederatedNetworkNotFoundException, AgentCommucationException,
            UnauthorizedRequestException, UnexpectedException {
        LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, systemUser, federatedNetworkId));
        FederatedNetworkOrder federatedNetwork = this.getFederatedNetwork(federatedNetworkId, systemUser);

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
                FederatedNetworkOrdersHolder.getInstance().removeOrder(federatedNetworkId);
                federatedNetwork.setOrderState(OrderState.DEACTIVATED);
            } else {
                throw new AgentCommucationException();
            }
        }
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, SystemUser systemUser)
            throws FederatedNetworkNotFoundException, UnauthorizedRequestException {

        FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(federatedNetworkId);

        if (federatedNetworkOrder != null) {
            if (federatedNetworkOrder.getSystemUser().equals(systemUser)) {
                return federatedNetworkOrder;
            }
            throw new UnauthorizedRequestException();
        }
        throw new FederatedNetworkNotFoundException(federatedNetworkId);
    }

    public Collection<InstanceStatus> getFederatedNetworksStatusByUser(SystemUser systemUser) {
        Collection<FederatedNetworkOrder> orders = FederatedNetworkOrdersHolder.getInstance().getActiveOrders().values();

        // Filter all orders of resourceType from systemUser that are not closed (closed orders have been deleted by
        // the user and should not be seen; they will disappear from the system).
        return orders.stream()
                    .filter(order -> order.getSystemUser().equals(systemUser))
                    .filter(order -> !order.getOrderState().equals(OrderState.DEACTIVATED))
                    .map(orderToInstanceStatus())
                    .collect(Collectors.toList());
    }

    public static Function<FederatedNetworkOrder, InstanceStatus> orderToInstanceStatus() {
        return order -> {
            InstanceState status = order.getInstanceStateFromOrderState();
            InstanceStatus instanceStatus = new InstanceStatus(order.getId(), order.getName(), RAS_NAME, status);
            return instanceStatus;
        };
    }
}
