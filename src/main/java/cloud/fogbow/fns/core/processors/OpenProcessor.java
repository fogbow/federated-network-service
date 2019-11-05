package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.linkedlists.ChainedList;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.OrderStateTransitioner;
import cloud.fogbow.fns.core.ServiceDriverConnector;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import org.apache.log4j.Logger;

public class OpenProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(cloud.fogbow.ras.core.processors.OpenProcessor.class);

    private Long sleepTime;

    private ChainedList<FederatedNetworkOrder> orders;

    public OpenProcessor(Long sleepTime) {
        this.sleepTime = sleepTime;
        this.orders = FederatedNetworkOrdersHolder.getInstance().getOrdersList(OrderState.OPEN);
    }

    @Override
    public void run() {
        while (true) {
            try {
                FederatedNetworkOrder order = this.orders.getNext();
                if (order != null) {
                    processOrder(order);
                } else {
                    this.orders.resetPointer();
                    Thread.sleep(this.sleepTime);
                }
            } catch (UnexpectedException e) {
                LOGGER.error("", e);
            } catch (InterruptedException e) {
                LOGGER.error(Messages.Exception.THREAD_HAS_BEEN_INTERRUPTED, e);
                break;
            }
        }
    }

    protected void processOrder(FederatedNetworkOrder order) throws UnexpectedException {
        // The order object synchronization is needed to prevent a race
        // condition on order access. For example: a user can delete an open
        // order while this method is trying to create the federated network.
        synchronized (order) {
            // Check if the order is still in the Open state (it could have been changed by another thread)
            if(!order.getOrderState().equals(OrderState.OPEN)) return;

            try {
                //The driver, with this call, must do all the operations needed to make the order able to move
                //to spawning state, according to its specification. If it can't be done, a FogbowException must be thrown.
                new ServiceDriverConnector(order.getServiceName()).getDriver().processOpen(order);
                OrderStateTransitioner.transition(order, OrderState.SPAWNING);
            } catch (FogbowException e) {
                LOGGER.error(e.getMessage(), e);
                OrderStateTransitioner.transition(order, OrderState.FAILED);
            }
        }
    }
}
