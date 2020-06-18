package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;

public class OrderStateTransitioner {
    public static void transition(FederatedNetworkOrder order, OrderState newState) throws InternalServerErrorException {
        synchronized (order) {
            doTransition(order, newState);
        }
    }

    private static void doTransition(FederatedNetworkOrder order, OrderState newState) throws InternalServerErrorException {
        OrderState currentState = order.getOrderState();

        if (currentState == newState) {
            // The order may have already been moved to the new state by another thread
            // In this case, there is nothing else to be done
            return;
        }

        SynchronizedDoublyLinkedList<FederatedNetworkOrder> origin = FederatedNetworkOrdersHolder.getInstance()
                .getOrdersList(currentState);
        SynchronizedDoublyLinkedList<FederatedNetworkOrder> destination = FederatedNetworkOrdersHolder.getInstance()
                .getOrdersList(newState);

        if (origin == null) {
            String message = String.format(Messages.Exception.UNABLE_TO_FIND_LIST_FOR_REQUESTS_IN_STATE_S, currentState);
            throw new InternalServerErrorException(message);
        } else if (destination == null) {
            String message = String.format(Messages.Exception.UNABLE_TO_FIND_LIST_FOR_REQUESTS_IN_STATE_S, newState);
            throw new InternalServerErrorException(message);
        } else {
            // The order may have already been removed from the origin list by another thread
            // In this case, there is nothing else to be done
            if (origin.removeItem(order)) {
                order.setOrderState(newState);
                destination.addItem(order);
            }
        }
    }
}
