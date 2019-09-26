package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.constants.HttpConstants;
import cloud.fogbow.common.constants.HttpMethod;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.common.util.HomeDir;
import cloud.fogbow.common.util.PropertiesUtil;
import cloud.fogbow.common.util.connectivity.HttpRequestClient;
import cloud.fogbow.common.util.connectivity.HttpResponse;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.CommonServiceDriver;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Properties;

public class DfnsServiceDriver extends CommonServiceDriver {

    private static final Logger LOGGER = Logger.getLogger(DfnsServiceDriver.class);
    public static final String SERVICE_NAME = "dfns";
    private static Properties properties = PropertiesHolder.getInstance().getProperties(SERVICE_NAME);
    public static final String VLAN_ID_SERVICE_URL = properties.getProperty(DfnsConfigurationPropertyKeys.VLAN_ID_SERVICE_URL_KEY);
    public static final String VLAN_ID_ENDPOINT = "/vlanId";
    public static final String ADD_AUTHORIZED_KEY_COMMAND_FORMAT = "touch ~/.ssh/authorized_keys && sed -i '1i%s' ~/.ssh/authorized_keys";
    public static final String PORT_TO_REMOVE_FORMAT = "gre-vm-%s-vlan-%s";
    public static final String REMOVE_TUNNEL_FROM_AGENT_TO_COMPUTE_FORMAT = "sudo ovs-vsctl del-port %s";
    private final String LOCAL_MEMBER_NAME = properties.getProperty(DfnsConfigurationPropertyKeys.LOCAL_MEMBER_NAME_KEY);
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
            //anymore. Thus, the only operation to be done is to change the
            //member's state to SUCCESS for each member. Once it can't result
            //in an Exception, it is not necessary to handle edge cases.
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
                dfnsAgentConfiguration = (SSAgentConfiguration) new DfnsServiceConnector(memberName).configureAgent(keys[PUBLIC_KEY_INDEX], SERVICE_NAME);
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
        } catch (IOException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    @Override
    public void cleanupAgent(FederatedNetworkOrder order, String hostIp) throws FogbowException {
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

    @Override
    public String getAgentIp() {
        return properties.getProperty(DfnsConfigurationPropertyKeys.HOST_IP_KEY);
    }

    private void addKeyToAgentAuthorizedPublicKeys(String publicKey) throws FogbowException {
        AgentCommunicatorUtil.executeAgentCommand(String.format(ADD_AUTHORIZED_KEY_COMMAND_FORMAT, publicKey), Messages.Exception.UNABLE_TO_ADD_KEY_IN_AGGENT, SERVICE_NAME);
    }

    @Override
    public SSAgentConfiguration doConfigureAgent(String publicKey) throws FogbowException{
        addKeyToAgentAuthorizedPublicKeys(publicKey);
        String defaultNetworkCidr = properties.getProperty(DfnsConfigurationPropertyKeys.DEFAULT_NETWORK_CIDR_KEY);

        String agentUser = properties.getProperty(DfnsConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPrivateIpAddress = properties.getProperty(DfnsConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY);
        String publicIpAddress = properties.getProperty(DfnsConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        return new SSAgentConfiguration(defaultNetworkCidr, agentUser, agentPrivateIpAddress, publicIpAddress);
    }

    public void removeAgentToComputeTunnel(FederatedNetworkOrder order, String hostIp) throws FogbowException {
        String removeTunnelCommand = String.format(REMOVE_TUNNEL_FROM_AGENT_TO_COMPUTE_FORMAT,
                (String.format(PORT_TO_REMOVE_FORMAT, hostIp, order.getVlanId())));

        AgentCommunicatorUtil.executeAgentCommand(removeTunnelCommand, Messages.Exception.UNABLE_TO_REMOVE_AGENT_TO_COMPUTE_TUNNEL, SERVICE_NAME);
    }

    private int acquireVlanId() throws FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        String acquireVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.GET, acquireVlanIdEndpoint, headers, new HashMap<>());

        if (response.getHttpCode() == HttpStatus.NOT_ACCEPTABLE.value()) {
            throw new NoVlanIdsLeftException();
        }

        VlanId vlanId = GsonHolder.getInstance().fromJson(response.getContent(), VlanId.class);
        return vlanId.vlanId;
    }

    private void releaseVlanId(int vlanId) throws FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);

        String jsonBody = GsonHolder.getInstance().toJson(new VlanId(vlanId));
        HashMap<String, String> body = GsonHolder.getInstance().fromJson(jsonBody, HashMap.class);

        String releaseVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.POST, releaseVlanIdEndpoint, headers, body);

        if (response.getHttpCode() == HttpStatus.NOT_FOUND.value()) {
            LOGGER.warn(String.format(Messages.Warn.UNABLE_TO_RELEASE_VLAN_ID, vlanId));
            throw new UnexpectedException(String.format(Messages.Warn.UNABLE_TO_RELEASE_VLAN_ID, vlanId));
        }
    }

    private class VlanId {

        private int vlanId;

        public VlanId(int vlanId) {
            this.vlanId = vlanId;
        }
    }

    private boolean isRemote() {
        return memberName != null && LOCAL_MEMBER_NAME.equals(memberName);
    }
}
