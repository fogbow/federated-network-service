package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.linkedlists.ChainedList;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.exceptions.AgentCommucationException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import org.apache.log4j.Logger;

public class ClosedProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(cloud.fogbow.ras.core.processors.ClosedProcessor.class);

    private final Long sleepTime;
    private ChainedList<FederatedNetworkOrder> orders;
    private FederatedNetworkOrderController orderController;

    public ClosedProcessor(FederatedNetworkOrderController orderController, Long sleepTime) {
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
            LOGGER.info(String.format(Messages.Info.DELETING_FEDERATED_NETWORK, order.toString()));
            boolean wasDeleted = AgentCommunicatorUtil.deleteFederatedNetwork(order.getCidr());
            if (wasDeleted || order.getOrderState() == OrderState.FAILED) {
                // If the state of the order is FAILED, this is because in the creation, it was not possible to
                // connect to the Agent. Thus, there is nothing to remove at the Agent, and an exception does not
                // need to be thrown.
                LOGGER.info(String.format(Messages.Info.DELETED_FEDERATED_NETWORK, order.toString()));

                this.orderController.deactivateOrder(order);
            } else {
                throw new UnexpectedException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK, new AgentCommucationException());
            }
        }
    }
}
