package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.linkedlists.ChainedList;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.OrderStateTransitioner;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SpawningProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(cloud.fogbow.ras.core.processors.ClosedProcessor.class);

    private final Long sleepTime;
    private ChainedList<FederatedNetworkOrder> orders;
    private FederatedNetworkOrderController orderController;

    public SpawningProcessor(FederatedNetworkOrderController orderController, Long sleepTime) {
        this.sleepTime = sleepTime;
        this.orders = FederatedNetworkOrdersHolder.getInstance().getClosedOrders();
        this.orderController = orderController;
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
        synchronized (order) {
            // Check if the order is still SPAWNING (its state may have been changed by another thread)
            if (order.getOrderState().equals(OrderState.SPAWNING)) {
                ConfigurationMode configurationMode = order.getConfigurationMode();

                switch(configurationMode) {
                    case VANILLA:
                        this.processVanillaOrder(order);
                        break;
                    case DFNS:
                        this.processDfnsOrder(order);
                        break;
                    default:
                        throw new RuntimeException(Messages.Exception.CONFIGURATION_MODE_NOT_IMPLEMENTED);
                }
            }
        }
    }

    private void processVanillaOrder(FederatedNetworkOrder order) throws UnexpectedException {
        ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                ConfigurationMode.VANILLA, order.getRequestingMember());
        MemberConfigurationState state = connector.configure(order);

        if (state == MemberConfigurationState.SUCCESS) {
            OrderStateTransitioner.transition(order, OrderState.FULFILLED);
        } else {
            OrderStateTransitioner.transition(order, OrderState.FAILED);
        }
    }

    private void processDfnsOrder(FederatedNetworkOrder order) throws UnexpectedException {
        for (String provider : order.getProviders().keySet()) {
            ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                    ConfigurationMode.DFNS, provider);
            MemberConfigurationState memberState = connector.configure(order);
            order.getProviders().put(provider, memberState);
        }

        OrderState nextOrderState = getNextOrderState(order.getProviders().values());
        OrderStateTransitioner.transition(order, nextOrderState);
    }

    private OrderState getNextOrderState(Collection<MemberConfigurationState> memberConfigurationStates) {
        OrderState orderState = OrderState.FULFILLED;
        boolean hasFail = false;
        boolean hasSuccess = false;

        for (MemberConfigurationState state : memberConfigurationStates) {
            if (state == MemberConfigurationState.PARTIAL_SUCCESS) {
                orderState = OrderState.PARTIALLY_FULFILLED;
                break;
            }  else if (state == MemberConfigurationState.SUCCESS) {
                hasSuccess = true;
            }  else {
                hasFail = true;
            }
        }

        if (orderState != OrderState.PARTIALLY_FULFILLED) {
            if (hasFail) {
                orderState = hasSuccess ? OrderState.PARTIALLY_FULFILLED : OrderState.FAILED;
            }
        }

        return orderState;
    }
}
