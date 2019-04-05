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

import java.util.*;

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
                        this.processDFNSOrder(order);
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

    private void processDFNSOrder(FederatedNetworkOrder order) throws UnexpectedException {
        Set<MemberConfigurationState> memberConfigurationStates = new HashSet<>();
        for (Map.Entry<String, MemberConfigurationState> provider : order.getProviders().entrySet()) {
            ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                    ConfigurationMode.DFNS, provider.getKey());
            MemberConfigurationState memberState = connector.configure(order);
            memberConfigurationStates.add(memberState);
        }

        OrderState nextOrderState = getNextOrderState(memberConfigurationStates);
        OrderStateTransitioner.transition(order, nextOrderState);
    }

    private OrderState getNextOrderState(Set<MemberConfigurationState> memberConfigurationStates) {
        OrderState orderState = OrderState.PARTIALLY_FULFILLED;

        if (memberConfigurationStates.contains(MemberConfigurationState.PARTIAL_SUCCESS)) return orderState;

        boolean hasFail = memberConfigurationStates.contains(null) || memberConfigurationStates.contains(MemberConfigurationState.FAILED);
        boolean hasSuccess = memberConfigurationStates.contains(MemberConfigurationState.SUCCESS);

        if (hasFail) {
            orderState = hasSuccess ? OrderState.PARTIALLY_FULFILLED : OrderState.FAILED;
        }

        return orderState;
    }
}
