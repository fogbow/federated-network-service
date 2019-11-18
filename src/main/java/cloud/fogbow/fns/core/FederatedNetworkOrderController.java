package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.InstanceNotFoundException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {
    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);

    // Federated Network methods
    public void activateOrder(FederatedNetworkOrder order) throws UnexpectedException {
        synchronized (order) {
            order.setOrderState(OrderState.OPEN);
            FederatedNetworkOrdersHolder.getInstance().putOrder(order);
        }
    }

    public FederatedNetworkOrder getFederatedNetwork(String orderId) throws FederatedNetworkNotFoundException {
        FederatedNetworkOrder requestedOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(orderId);
        if (requestedOrder == null) {
            throw new FederatedNetworkNotFoundException(orderId);
        }
        return requestedOrder;
    }

    public void deleteFederatedNetwork(FederatedNetworkOrder order)
            throws FogbowException {
        synchronized (order) {
            if (!(order.getOrderState().equals(OrderState.CLOSED) ||
                    order.getOrderState().equals(OrderState.DEACTIVATED))) {
                LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, order.getId()));

                if (!order.isAssignedIpsEmpty()) {
                    throw new NotEmptyFederatedNetworkException();
                }

                LOGGER.info(String.format(Messages.Info.DELETING_FEDERATED_NETWORK, order.toString()));

                // If the state of the order is still FAILED, this is because in the creation, it was not possible to
                // connect to the Agent. Thus, there is nothing to remove at the Agent, and an exception does not
                // need to be thrown.
                OrderStateTransitioner.transition(order, OrderState.CLOSED);
            } else {
                String message = String.format(Messages.Error.REQUEST_ALREADY_CLOSED, order.getId());
                throw new InstanceNotFoundException(message);
            }
        }
    }

    public Collection<InstanceStatus> getInstancesStatus(SystemUser systemUser) {
        Collection<InstanceStatus> instanceStatusList = new ArrayList<>();
        Collection<FederatedNetworkOrder> allOrders = getAllActiveOrdersFromUser(systemUser);

        for (FederatedNetworkOrder order : allOrders) {
            synchronized (order) {
                if (order.getOrderState() == OrderState.CLOSED || order.getOrderState() == OrderState.DEACTIVATED) {
                    // The order might have been closed or deactivated between the time the list of orders were
                    // fetched by the getAllOrders() call above and now.
                    continue;
                }
                InstanceStatus instanceStatus = new InstanceStatus(
                        order.getId(),
                        order.getName(),
                        order.getProvider(),
                        order.getInstanceStateFromOrderState());
                instanceStatusList.add(instanceStatus);
            }
        }

        return instanceStatusList;
    }

    private Collection<FederatedNetworkOrder> getAllActiveOrdersFromUser(SystemUser systemUser) {
        Collection<FederatedNetworkOrder> allOrders = FederatedNetworkOrdersHolder.getInstance().getActiveOrders().values();
        // Filter all orders of from the user systemUser that are not closed (closed orders have been deleted
        // by the user and should not be seen; they will disappear from the system as soon as the closedProcessor
        // thread process them) or deactivated.
        Collection<FederatedNetworkOrder> requestedOrders = allOrders.stream()
                .filter(order -> order.getSystemUser().equals(systemUser))
                .filter(order -> !order.getOrderState().equals(OrderState.DEACTIVATED))
                .filter(order -> !order.getOrderState().equals(OrderState.CLOSED)).collect(Collectors.toList());

        return requestedOrders;
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
