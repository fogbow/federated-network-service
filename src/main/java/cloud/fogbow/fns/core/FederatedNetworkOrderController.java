package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.InstanceNotFoundException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.AgentCommucationException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.InstanceState;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {
    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);
    public static final String RAS_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.LOCAL_MEMBER_ID_KEY);

    // Federated Network methods
    public FederatedNetworkOrder getFederatedNetwork(String orderId) throws InstanceNotFoundException {
        FederatedNetworkOrder requestedOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(orderId);
        if (requestedOrder == null) {
            throw new InstanceNotFoundException();
        }
        return requestedOrder;
    }

    public void addFederatedNetwork(FederatedNetworkOrder federatedNetwork, SystemUser systemUser)
            throws InvalidCidrException, UnexpectedException {
        synchronized (federatedNetwork) {
            federatedNetwork.setSystemUser(systemUser);

            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidr());

            if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
                LOGGER.error(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
                throw new InvalidCidrException(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
            }

            this.activateOrder(federatedNetwork);
        }
    }

    public void deleteFederatedNetwork(FederatedNetworkOrder federatedNetwork)
            throws NotEmptyFederatedNetworkException, UnexpectedException {
        LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, federatedNetwork.getId()));

        if (!federatedNetwork.getComputeIdsAndIps().isEmpty()) {
            throw new NotEmptyFederatedNetworkException();
        }

        synchronized (federatedNetwork) {
            LOGGER.info(String.format(Messages.Info.DELETING_FEDERATED_NETWORK, federatedNetwork.toString()));
            boolean wasDeleted = AgentCommunicatorUtil.deleteFederatedNetwork(federatedNetwork.getCidr());
            if (wasDeleted || federatedNetwork.getOrderState() == OrderState.FAILED) {
                // If the state of the order is FAILED, this is because in the creation, it was not possible to
                // connect to the Agent. Thus, there is nothing to remove at the Agent, and an exception does not
                // need to be thrown.
                LOGGER.info(String.format(Messages.Info.DELETED_FEDERATED_NETWORK, federatedNetwork.toString()));
                OrderStateTransitioner.transition(federatedNetwork, OrderState.CLOSED);
            } else {
                throw new UnexpectedException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK, new AgentCommucationException());
            }
        }
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

    public void activateOrder(FederatedNetworkOrder order) throws UnexpectedException {
        synchronized (order) {
            order.setOrderState(OrderState.OPEN);
            FederatedNetworkOrdersHolder.getInstance().insertNewOrder(order);
        }
    }

    public void deactivateOrder(FederatedNetworkOrder order) throws UnexpectedException {
        synchronized (order) {
            if (!order.getOrderState().equals(OrderState.CLOSED)) {
                String message = Messages.Exception.ORDER_SHOULD_BE_CLOSED_BEFORE_DEACTIVATED;
                throw new RuntimeException(String.format(message, order.getId()));
            }
            FederatedNetworkOrdersHolder.getInstance().removeOrder(order);
        }
    }
}
