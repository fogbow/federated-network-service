package cloud.fogbow.fns.core.drivers.vanilla;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.HomeDir;
import cloud.fogbow.common.util.PropertiesUtil;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.CommonServiceDriver;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.drivers.dfns.AgentConfiguration;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class VanillaServiceDriver extends CommonServiceDriver {

    private static final Logger LOGGER = Logger.getLogger(VanillaServiceDriver.class);
    public static final String SERVICE_NAME = "vanilla";
    private static Properties properties = PropertiesHolder.getInstance().getProperties(SERVICE_NAME);
    private final int DEFAULT_VLAN_ID = -1;
    public VanillaServiceDriver() {
    }

    @Override
    public void processOpen(FederatedNetworkOrder order) {
        order.setVlanId(DEFAULT_VLAN_ID);
    }

    @Override
    public void processSpawning(FederatedNetworkOrder order) throws FogbowException {
        try {
            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(order.getCidr());
            AgentCommunicatorUtil.createFederatedNetwork(order.getCidr(), subnetInfo.getLowAddress());
        } catch (FogbowException e) {
            LOGGER.error(e.getMessage(), e);
            throw new FogbowException(e.getMessage());
        }
    }

    @Override
    public void processClosed(FederatedNetworkOrder order) throws FogbowException {
        for (String provider : order.getProviders().keySet()) {
            if (!order.getProviders().get(provider).equals(MemberConfigurationState.REMOVED)) {
               remove(order, provider);
            }
        }
    }

    private void remove(FederatedNetworkOrder order, String provider) throws FogbowException{
        try {
            AgentCommunicatorUtil.deleteFederatedNetwork(order.getCidr());
            order.getProviders().put(provider, MemberConfigurationState.REMOVED);
        } catch(FogbowException ex) {
            throw new UnexpectedException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK, ex);
        }
    }

    @Override
    public AgentConfiguration configureAgent(String provider) {
        return null;
    }
    @Override
    public AgentConfiguration doConfigureAgent(String publicKey) { return null; }

    @Override
    public UserData getComputeUserData(AgentConfiguration agentConfiguration, FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException {
        try {
            return FederatedComputeUtil.getVanillaUserData(instanceIp, order.getCidr());
        } catch (IOException e) {
            throw new FogbowException(e.getMessage(), e);
        }
    }

    @Override
    public void cleanupAgent(FederatedNetworkOrder order, String hostIp){

    }

    @Override
    public String getAgentIp() {
        return properties.getProperty(VanillaConfigurationPropertyKeys.HOST_IP_KEY);
    }
}
