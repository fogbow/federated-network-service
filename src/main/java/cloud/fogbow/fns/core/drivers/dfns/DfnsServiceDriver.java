package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.GeneralServiceDriver;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.intercomponent.serviceconnector.*;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DfnsServiceDriver extends GeneralServiceDriver {

    private static final Logger LOGGER = Logger.getLogger(DfnsServiceDriver.class);

    public static final String ADD_AUTHORIZED_KEY_COMMAND_FORMAT = "touch ~/.ssh/authorized_keys && sed -i '1i%s' ~/.ssh/authorized_keys";
    public static final String PORT_TO_REMOVE_FORMAT = "gre-vm-%s-vlan-%s";
    public static final String REMOVE_TUNNEL_FROM_AGENT_TO_COMPUTE_FORMAT = "sudo ovs-vsctl del-port %s";
    private final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.LOCAL_MEMBER_NAME_KEY);
    private String memberName;


    public DfnsServiceDriver() {
    }

    public DfnsServiceDriver(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public void processOpen(FederatedNetworkOrder order) throws FogbowException {
        try {
            order.setVlanId(acquireVlanId());
        } catch(FogbowException ex) {
            LOGGER.error(Messages.Exception.NO_MORE_VLAN_IDS_AVAILABLE);
            throw new FogbowException(ex.getMessage());
        }
    }

    @Override
    public void processSpawning(FederatedNetworkOrder order) {
        for (String provider : order.getProviders().keySet()) {
            //Here we used to run a script responsible for configure each
            //provider, but once we do that in deployment time it is not necessary
            //anymore.
            order.getProviders().put(provider, MemberConfigurationState.SUCCESS);
        }
    }

    @Override
    public void processClosed(FederatedNetworkOrder order) throws FogbowException {
        for (String provider : order.getProviders().keySet()) {
            order.getProviders().put(provider, MemberConfigurationState.REMOVED);
        }
        releaseVlanId(order.getVlanId());
        order.setVlanId(-1);
    }

    @Override
    public AgentConfiguration configureAgent() throws FogbowException {
        try {
            SSAgentConfiguration dfnsAgentConfiguration = null;
            String[] keys = generateSshKeyPair();
            if(!isRemote()) {
                dfnsAgentConfiguration = doConfigureAgent(keys[PUBLIC_KEY_INDEX]);
                dfnsAgentConfiguration.setPublicKey(keys[PUBLIC_KEY_INDEX]);
            } else {
                dfnsAgentConfiguration = (SSAgentConfiguration) new DfnsServiceConnector(memberName).configureAgent(keys[PUBLIC_KEY_INDEX]);
            }
            dfnsAgentConfiguration.setPrivateKey(keys[PRIVATE_KEY_INDEX]);
            return dfnsAgentConfiguration;
        } catch(FogbowException ex) {
            LOGGER.error(ex.getMessage());
            throw new FogbowException(ex.getMessage());
        }
    }

    @Override
    public UserData getComputeUserData(AgentConfiguration configuration, FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException {
        try {
            SSAgentConfiguration dfnsAgentConfiguration = (SSAgentConfiguration) configuration;
            String privateIpAddress = dfnsAgentConfiguration.getPrivateIpAddress();
            return FederatedComputeUtil.getDfnsUserData(dfnsAgentConfiguration, instanceIp, privateIpAddress,
                    order.getVlanId(), dfnsAgentConfiguration.getPrivateKey());
        } catch (IOException | GeneralSecurityException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    @Override
    public void cleanup(FederatedNetworkOrder order, String hostIp) throws FogbowException {
        try {
            if(!isRemote()) {
                removeAgentToComputeTunnel(order, hostIp);
            } else {
                new DfnsServiceConnector(memberName).removeAgentToComputeTunnel(order, hostIp);
            }
        } catch (FogbowException ex) {
            LOGGER.error(ex.getMessage());
            throw new FogbowException(ex.getMessage());
        }
    }

    private void addKeyToAgentAuthorizedPublicKeys(String publicKey) throws FogbowException {
        AgentCommunicatorUtil.executeAgentCommand(String.format(ADD_AUTHORIZED_KEY_COMMAND_FORMAT, publicKey), Messages.Exception.UNABLE_TO_ADD_KEY_IN_AGGENT);
    }

    public SSAgentConfiguration doConfigureAgent(String publicKey) throws FogbowException{
        addKeyToAgentAuthorizedPublicKeys(publicKey);
        String defaultNetworkCidr = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.DEFAULT_NETWORK_CIDR_KEY, ConfigurationMode.DFNS);

        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPrivateIpAddress = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY);
        String publicIpAddress = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        return new SSAgentConfiguration(defaultNetworkCidr, agentUser, agentPrivateIpAddress, publicIpAddress);
    }

    public void removeAgentToComputeTunnel(FederatedNetworkOrder order, String hostIp) throws FogbowException {
        String removeTunnelCommand = String.format(REMOVE_TUNNEL_FROM_AGENT_TO_COMPUTE_FORMAT,
                (String.format(PORT_TO_REMOVE_FORMAT, hostIp, order.getVlanId())));

        AgentCommunicatorUtil.executeAgentCommand(removeTunnelCommand, Messages.Exception.UNABLE_TO_REMOVE_AGENT_TO_COMPUTE_TUNNEL);
    }

    private boolean isRemote() {
        return LOCAL_MEMBER_NAME.equals(memberName);
    }
}
