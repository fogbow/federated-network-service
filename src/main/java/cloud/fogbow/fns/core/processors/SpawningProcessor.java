package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.models.linkedlists.ChainedList;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.OrderStateTransitioner;
import cloud.fogbow.fns.core.ServiceDriverConnector;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import org.apache.log4j.Logger;

public class SpawningProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SpawningProcessor.class);

    private final Long sleepTime;
    private ChainedList<FederatedNetworkOrder> orders;

    public SpawningProcessor(Long sleepTime) throws InternalServerErrorException {
        this.sleepTime = sleepTime;
        this.orders = FederatedNetworkOrdersHolder.getInstance().getOrdersList(OrderState.SPAWNING);
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
            } catch (FogbowException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                LOGGER.error(Messages.Log.THREAD_HAS_BEEN_INTERRUPTED, e);
                break;
            }
        }
    }

    protected void processOrder(FederatedNetworkOrder order) throws FogbowException {
        synchronized (order) {
            // Check if the order is still SPAWNING (its state may have been changed by another thread)
            if(!order.getOrderState().equals(OrderState.SPAWNING)) return;

            try {
                //The driver, with this call, must do all the operations needed to make the order able to move
                //to fulfilled state, according to its specification. If it can't be done a FogbowException
                //must be thrown.
                new ServiceDriverConnector(order.getServiceName()).getDriver().processSpawning(order);
                OrderStateTransitioner.transition(order, OrderState.FULFILLED);
            } catch (FogbowException ex) {
                OrderStateTransitioner.transition(order, OrderState.FAILED);
            }

        }
    }
}
