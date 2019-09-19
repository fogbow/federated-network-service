package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.GeneralServiceDriver;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.serviceconnector.AgentConfiguration;
import cloud.fogbow.fns.core.serviceconnector.RemoteDfnsServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DfnsServiceDriver extends GeneralServiceDriver {

    private static final Logger LOGGER = Logger.getLogger(DfnsServiceDriver.class);

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
            order.setVlanId(dfnsServiceConnetor.acquireVlanId());
        } catch(FogbowException ex) {
            LOGGER.error(Messages.Exception.NO_MORE_VLAN_IDS_AVAILABLE);
            throw new FogbowException(ex.getMessage());
        }
    }

    @Override
    public void processSpawning(FederatedNetworkOrder order) throws FogbowException {
        for (String provider : order.getProviders().keySet()) {
            MemberConfigurationState memberState = dfnsServiceConnetor.configure(order);
            order.getProviders().put(provider, memberState);
        }
    }

    @Override
    public void processClosed(FederatedNetworkOrder order) throws FogbowException {
        if (order.getOrderState().equals(OrderState.FAILED)) {
            for (String provider : federatedNetwork.getProviders().keySet()) {
                ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                        federatedNetwork.getConfigurationMode(), provider);
                if (!federatedNetwork.getProviders().get(provider).equals(MemberConfigurationState.REMOVED)) {
                    if (connector.remove(federatedNetwork)) {
                        federatedNetwork.getProviders().put(provider, MemberConfigurationState.REMOVED);
                    }
                }
            }

            boolean providersRemovedTheConfiguration = allProvidersRemovedTheConfiguration(federatedNetwork.getProviders().values());
            if (!providersRemovedTheConfiguration) {
                LOGGER.info(String.format(Messages.Info.DELETED_FEDERATED_NETWORK, federatedNetwork.toString()));
                throw new UnexpectedException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK, new AgentCommucationException());
            }

            ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                    federatedNetwork.getConfigurationMode(), LOCAL_MEMBER_NAME);
            connector.releaseVlanId(federatedNetwork.getVlanId());
            federatedNetwork.setVlanId(-1);
        }
    }

    @Override
    public AgentConfiguration configureAgent() {
        try {
            AgentConfiguration dfnsAgentConfiguration = null;
            String[] keys = generateSshKeyPair();
            if(!isRemote()) {
                dfnsAgentConfiguration = doConfigureAgent(keys[PUBLIC_KEY_INDEX]);
                dfnsAgentConfiguration.setPublicKey(keys[PUBLIC_KEY_INDEX]);
            } else {
                dfnsAgentConfiguration = new RemoteDfnsServiceConnector(memberName).configureAgent(keys[PUBLIC_KEY_INDEX]);
            }
            dfnsAgentConfiguration.setPrivateKey(keys[PRIVATE_KEY_INDEX]);
            return dfnsAgentConfiguration;
        } catch(FogbowException ex) {

        }
    }

    private void addKeyToAgentAuthorizedPublicKeys(String publicKey) throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        SSHClient client = new SSHClient();
        client.addHostKeyVerifier((arg0, arg1, arg2) -> true);

        try {
            try {
                // connects to the DMZ host
                client.connect(agentPublicIp, AGENT_SSH_PORT);

                // authorizes using the DMZ private key
                client.authPublickey(agentUser, permissionFilePath);

                try (Session session = client.startSession()) {
                    Session.Command c = session.exec(String.format(ADD_AUTHORIZED_KEY_COMMAND_FORMAT, publicKey));

                    // waits for the command to finish
                    c.join();

                    return c.getExitStatus() == SUCCESS_EXIT_CODE;
                }
            } finally {
                client.disconnect();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    public AgentConfiguration doConfigureAgent(String publicKey) throws FogbowException{
        addKeyToAgentAuthorizedPublicKeys(publicKey);
        String defaultNetworkCidr = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.DEFAULT_NETWORK_CIDR_KEY);

        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPrivateIpAddress = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY);
        String publicIpAddress = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        return new AgentConfiguration(defaultNetworkCidr, agentUser, agentPrivateIpAddress, publicIpAddress);
    }

    @Override
    public UserData getComputeUserData(AgentConfiguration configuration, FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException {
        try {
            String privateIpAddress = configuration.getPrivateIpAddress();
            return FederatedComputeUtil.getDfnsUserData(configuration, instanceIp, privateIpAddress,
                    order.getVlanId(), configuration.getPrivateKey());
        } catch (IOException | GeneralSecurityException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    @Override
    public void cleanup(FederatedNetworkOrder order, String hostIp) throws FogbowException {

    }

    private boolean isRemote() {
        return LOCAL_MEMBER_NAME.equals(memberName);
    }
}
