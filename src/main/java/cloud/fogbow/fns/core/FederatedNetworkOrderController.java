package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.InstanceNotFoundException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.*;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {
    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);

    public static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.LOCAL_MEMBER_NAME_KEY);

    // Federated Network methods
    public FederatedNetworkOrder getFederatedNetwork(String orderId) throws FederatedNetworkNotFoundException {
        FederatedNetworkOrder requestedOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(orderId);
        if (requestedOrder == null) {
            throw new FederatedNetworkNotFoundException(orderId);
        }
        return requestedOrder;
    }

    public void activateOrder(FederatedNetworkOrder order) throws UnexpectedException {
        synchronized (order) {
            order.setOrderState(OrderState.OPEN);
            FederatedNetworkOrdersHolder.getInstance().insertNewOrder(order);
        }
    }

    public void deleteFederatedNetwork(FederatedNetworkOrder federatedNetwork)
            throws FogbowException {
        synchronized (federatedNetwork) {
            if (!(federatedNetwork.getOrderState().equals(OrderState.CLOSED) ||
                    federatedNetwork.getOrderState().equals(OrderState.DEACTIVATED))) {
                LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, federatedNetwork.getId()));

                if (!federatedNetwork.isAssignedIpsEmpty()) {
                    throw new NotEmptyFederatedNetworkException();
                }

                LOGGER.info(String.format(Messages.Info.DELETING_FEDERATED_NETWORK, federatedNetwork.toString()));

                // If the state of the order is still FAILED, this is because in the creation, it was not possible to
                // connect to the Agent. Thus, there is nothing to remove at the Agent, and an exception does not
                // need to be thrown.
                OrderStateTransitioner.transition(federatedNetwork, OrderState.CLOSED);
            } else {
                String message = String.format(Messages.Error.REQUEST_ALREADY_CLOSED, federatedNetwork.getId());
                throw new InstanceNotFoundException(message);
            }
        }
    }

    public Collection<InstanceStatus> getFederatedNetworksStatusByUser(SystemUser systemUser) {
        Collection<FederatedNetworkOrder> orders = FederatedNetworkOrdersHolder.getInstance().getActiveOrders().values();

        // Filter all orders from systemUser that are not closed (closed orders have been deleted by
        // the user and should not be seen; they will disappear from the system) or deactivated.
        return orders.stream()
                .filter(order -> order.getSystemUser().equals(systemUser))
                .filter(order -> !order.getOrderState().equals(OrderState.CLOSED))
                .filter(order -> !order.getOrderState().equals(OrderState.DEACTIVATED))
                .map(orderToInstanceStatus())
                .collect(Collectors.toList());
    }

    public static Function<FederatedNetworkOrder, InstanceStatus> orderToInstanceStatus() {
        return order -> {
            InstanceState status = order.getInstanceStateFromOrderState();
            InstanceStatus instanceStatus = new InstanceStatus(order.getId(), order.getName(), LOCAL_MEMBER_NAME, status);
            return instanceStatus;
        };
    }

    public void deactivateOrder(FederatedNetworkOrder order) throws UnexpectedException {
        synchronized (order) {
            if (!order.getOrderState().equals(OrderState.CLOSED)) {
                String message = Messages.Exception.ORDER_SHOULD_BE_CLOSED_BEFORE_DEACTIVATED;
                throw new UnexpectedException(String.format(message, order.getId()));
            }
            FederatedNetworkOrdersHolder.getInstance().removeOrder(order);
        }
    }
}
