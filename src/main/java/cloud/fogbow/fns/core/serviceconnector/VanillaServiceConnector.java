package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.api.parameters.Compute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.ras.core.models.UserData;
import cloud.fogbow.ras.core.models.orders.ComputeOrder;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class VanillaServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(VanillaServiceConnector.class);
    public static final String PRE_SHARED_KEY = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_PRE_SHARED_KEY_KEY);
    public static final String AGENT_IP_ADDRESS = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

    @Override
    public int acquireVlanId() {
        // not needed for vanilla
        return -1;
    }

    @Override
    public boolean releaseVlanId(int vlandId) {
        // not needed for vanilla
        return true;
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

    @Override
    public boolean removeAgentToComputeTunnel(String hostIp, int vlanId) throws UnexpectedException {
        // not needed for vanilla
        return true;
    }

    @Override
    public UserData getTunnelCreationInitScript(String federatedIp, Compute compute, FederatedNetworkOrder order) throws UnexpectedException {
        try {
            return FederatedComputeUtil.getVanillaUserData(federatedIp, order.getCidr());
        } catch (IOException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }
}
