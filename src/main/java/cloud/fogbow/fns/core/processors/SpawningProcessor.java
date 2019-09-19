package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.models.linkedlists.ChainedList;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.OrderStateTransitioner;
import cloud.fogbow.fns.core.drivers.ServiceDriverFactory;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import org.apache.log4j.Logger;

import java.util.Collection;


public class SpawningProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(cloud.fogbow.ras.core.processors.ClosedProcessor.class);

    private final Long sleepTime;
    private ChainedList<FederatedNetworkOrder> orders;

    public SpawningProcessor(Long sleepTime) {
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
                LOGGER.error("", e);
            } catch (InterruptedException e) {
                LOGGER.error(Messages.Exception.THREAD_HAS_BEEN_INTERRUPTED, e);
                break;
            }
        }
    }

    protected void processOrder(FederatedNetworkOrder order) throws FogbowException {
        synchronized (order) {
            // Check if the order is still SPAWNING (its state may have been changed by another thread)
            if(!order.getOrderState().equals(OrderState.SPAWNING)) return;

            ConfigurationMode configurationMode = order.getConfigurationMode();
            try {
                ServiceDriverFactory.getInstance().getServiceDriver(configurationMode).processSpawning(order);
                OrderStateTransitioner.transition(order,
                    configurationMode.equals(ConfigurationMode.DFNS) ? getNextOrderState(order.getProviders().values()) : OrderState.FULFILLED);
            } catch (FogbowException ex) {
                OrderStateTransitioner.transition(order, OrderState.FAILED);
            }

        }
    }

    private OrderState getNextOrderState(Collection<MemberConfigurationState> memberConfigurationStates) {
        OrderState orderState = OrderState.PARTIALLY_FULFILLED;

        if (memberConfigurationStates.contains(MemberConfigurationState.PARTIAL_SUCCESS)) return orderState;

        boolean hasFail = memberConfigurationStates.contains(null) || memberConfigurationStates.contains(MemberConfigurationState.FAILED);
        boolean hasSuccess = memberConfigurationStates.contains(MemberConfigurationState.SUCCESS);

        if (hasFail) {
            orderState = hasSuccess ? OrderState.PARTIALLY_FULFILLED : OrderState.FAILED;
        } else {
            orderState = OrderState.FULFILLED;
        }

        return orderState;
    }
}
