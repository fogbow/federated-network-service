package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

public class VanillaServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(VanillaServiceConnector.class);

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        return -1;
    }

    @Override
    public void releaseVlanId(int vlandId) {
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        MemberConfigurationState result = MemberConfigurationState.FAILED;
        try {
            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(order.getCidr());
            boolean successfulConfiguration = AgentCommunicatorUtil.createFederatedNetwork(order.getCidr(), subnetInfo.getLowAddress());
            if (successfulConfiguration) {
                result = MemberConfigurationState.SUCCESS;
            }
        } catch (InvalidCidrException e) {
            LOGGER.error(Messages.Exception.INVALID_CIDR, e);
        }
        return result;
    }

    @Override
    public boolean remove(FederatedNetworkOrder order) {
        return AgentCommunicatorUtil.deleteFederatedNetwork(order.getCidr());
    }
}
