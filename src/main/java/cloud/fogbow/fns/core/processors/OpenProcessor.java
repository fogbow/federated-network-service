package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.linkedlists.ChainedList;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.OrderStateTransitioner;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

public class OpenProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(cloud.fogbow.ras.core.processors.OpenProcessor.class);

    private Long sleepTime;
    private ChainedList<FederatedNetworkOrder> orders;

    public OpenProcessor(Long sleepTime) {
        this.sleepTime = sleepTime;
        this.orders = FederatedNetworkOrdersHolder.getInstance().getOpenOrders();
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
            } catch (InvalidCidrException e) {
                LOGGER.error("", e);
            } catch (UnexpectedException e) {
                LOGGER.error("", e);
            } catch (InterruptedException e) {
                LOGGER.error(Messages.Exception.THREAD_HAS_BEEN_INTERRUPTED, e);
                break;
            }
        }
    }

    private void processOrder(FederatedNetworkOrder federatedNetwork) throws UnexpectedException, InvalidCidrException {
        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidr());
        boolean successfullyCreated = AgentCommunicatorUtil.createFederatedNetwork(
                federatedNetwork.getCidr(),subnetInfo.getLowAddress());
        if (successfullyCreated) {
            OrderStateTransitioner.transition(federatedNetwork, OrderState.FULFILLED);
        } else {
            OrderStateTransitioner.transition(federatedNetwork, OrderState.FAILED);
        }
    }
}
