package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;

public class OpenProcessor {

    public void processOrder(FederatedNetworkOrder federatedNetwork) throws UnexpectedException, InvalidCidrException {
        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidr());
        boolean successfullyCreated = AgentCommunicatorUtil.createFederatedNetwork(
                federatedNetwork.getCidr(),subnetInfo.getLowAddress());

        if (successfullyCreated) {
            federatedNetwork.setOrderState(OrderState.FULFILLED);
        } else {
            federatedNetwork.setOrderState(OrderState.FAILED);
        }
    }
}
