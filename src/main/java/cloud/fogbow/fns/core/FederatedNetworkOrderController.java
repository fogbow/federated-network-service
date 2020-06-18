package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.InstanceNotFoundException;
import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.exceptions.UnacceptableOperationException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.model.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {
    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);

    // Federated Network methods
    public void activateOrder(FederatedNetworkOrder order) throws InternalServerErrorException {
        synchronized (order) {
            order.setOrderState(OrderState.OPEN);
            FederatedNetworkOrdersHolder.getInstance().putOrder(order);
        }
    }

    public FederatedNetworkOrder getFederatedNetwork(String orderId) throws InstanceNotFoundException, InternalServerErrorException {
        FederatedNetworkOrder requestedOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(orderId);
        if (requestedOrder == null) {
            throw new InstanceNotFoundException(String.format(Messages.Exception.UNABLE_TO_FIND_FEDERATED_NETWORK_S, orderId));
        }
        return requestedOrder;
    }

    public void deleteFederatedNetwork(FederatedNetworkOrder order) throws FogbowException {
        synchronized (order) {
            if (!(order.getOrderState().equals(OrderState.CLOSED) ||
                    order.getOrderState().equals(OrderState.DEACTIVATED))) {
                LOGGER.info(String.format(Messages.Log.INITIALIZING_DELETE_METHOD_S, order.getId()));

                if (!order.isAssignedIpsEmpty()) {
                    throw new UnacceptableOperationException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK);
                }

                LOGGER.info(String.format(Messages.Log.DELETING_FEDERATED_NETWORK_S, order.toString()));

                // If the state of the order is still FAILED, this is because in the creation, it was not possible to
                // connect to the Agent. Thus, there is nothing to remove at the Agent, and an exception does not
                // need to be thrown.
                OrderStateTransitioner.transition(order, OrderState.CLOSED);
            } else {
                throw new InstanceNotFoundException(String.format(Messages.Log.REQUEST_S_ALREADY_CLOSED, order.getId()));
            }
        }
    }

    public Collection<InstanceStatus> getInstancesStatus(SystemUser systemUser) throws InternalServerErrorException {
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

    private Collection<FederatedNetworkOrder> getAllActiveOrdersFromUser(SystemUser systemUser) throws InternalServerErrorException {
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

    public void deactivateOrder(FederatedNetworkOrder order) throws InternalServerErrorException {
        synchronized (order) {
            if (!order.getOrderState().equals(OrderState.CLOSED)) {
                String message = Messages.Exception.ORDER_S_SHOULD_BE_CLOSED_BEFORE_DEACTIVATED;
                throw new InternalServerErrorException(String.format(message, order.getId()));
            }
            FederatedNetworkOrdersHolder.getInstance().removeOrder(order);
        }
    }
}
