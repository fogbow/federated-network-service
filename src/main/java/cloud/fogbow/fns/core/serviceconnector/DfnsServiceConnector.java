package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.constants.HttpConstants;
import cloud.fogbow.common.constants.HttpMethod;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.common.util.connectivity.HttpRequestClient;
import cloud.fogbow.common.util.connectivity.HttpResponse;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.BashScriptRunner;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class DfnsServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DfnsServiceConnector.class);

    public static final int SUCCESS_EXIT_CODE = 0;

    protected BashScriptRunner runner;

    public static final String SCRIPT_TARGET_PATH = "/tmp/";
    public static final String VLAN_ID_SERVICE_URL = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.VLAN_ID_SERVICE_URL);
    public static final String VLAN_ID_ENDPOINT = "/vlanId";

    public DfnsServiceConnector() {
    }

    public DfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException, FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        String acquireVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.GET, acquireVlanIdEndpoint, headers, new HashMap<>());

        VlanId vlanId = GsonHolder.getInstance().fromJson(response.getContent(), VlanId.class);
        return vlanId.vlanId;
    }

    @Override
    public boolean releaseVlanId(int vlanId) throws FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);

        String jsonBody = GsonHolder.getInstance().toJson(new VlanId(vlanId));
        HashMap<String, String> body = GsonHolder.getInstance().fromJson(jsonBody, HashMap.class);

        String releaseVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.POST, releaseVlanIdEndpoint, headers, body);

        return response.getHttpCode() == HttpStatus.OK.value();
    }

    @Override
    public final UserData getTunnelCreationInitScript(String federatedIp, FederatedCompute compute, FederatedNetworkOrder order) throws UnexpectedException {
        boolean copiedScriptSuccessfully = copyScriptForTunnelFromAgentToComputeCreationIntoAgent();
        if (!copiedScriptSuccessfully) {
            throw new UnexpectedException(Messages.Exception.UNABLE_TO_COPY_SCRIPT_TO_AGENT);
        }

        try {
            KeyPair keyPair = CryptoUtil.generateKeyPair();

            addKeyToAgentAuthorizedPublicKeys(serializePublicKey(keyPair.getPublic()));

            DfnsAgentConfiguration dfnsAgentConfiguration = getDfnsAgentConfiguration(CryptoUtil.savePublicKey(keyPair.getPublic()));
            String privateIpAddress = dfnsAgentConfiguration.getPrivateIpAddress();
            return FederatedComputeUtil.getDfnsUserData(dfnsAgentConfiguration, federatedIp, privateIpAddress, order.getVlanId(), keyPair.getPrivate());
        } catch (IOException | GeneralSecurityException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    /**
     * Sends the script that creates a tunnel from the agent to the compute to the agent. It will be called
     * by the newly created compute that has just created the opposite tunnel (compute -> agent).
     */
    public boolean copyScriptForTunnelFromAgentToComputeCreationIntoAgent() throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        String tunnelScriptCreationPath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.CREATE_TUNNEL_FROM_AGENT_TO_COMPUTE_SCRIPT_PATH);

        String sshCredentials = agentUser + "@" + agentPublicIp;
        String scpPath = sshCredentials + ":" + SCRIPT_TARGET_PATH;

        BashScriptRunner.Output output = this.runner.runtimeRun("scp", "-o", "UserKnownHostsFile=/dev/null", "-o", "StrictHostKeyChecking=no", "-i", permissionFilePath, tunnelScriptCreationPath, scpPath);

        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    /**
     * Adds the provided <tt>publicKey</tt> to the .authorized_keys of the DFNS agent.
     *
     * @param publicKey
     * @return
     * @throws UnexpectedException
     */
    public abstract boolean addKeyToAgentAuthorizedPublicKeys(String publicKey) throws UnexpectedException;

    // TODO we might want to include the cloud here, since RAS is multi cloud and there might be multiple default networks
    public abstract DfnsAgentConfiguration getDfnsAgentConfiguration(String serializedPublicKey) throws UnknownHostException, UnexpectedException;

    private String serializePublicKey(PublicKey publicKey) throws GeneralSecurityException {
        return String.format("ssh-rsa %s", CryptoUtil.savePublicKey(publicKey));
    }

    protected Collection<String> getIpAddresses(Collection<String> serverNames) throws UnknownHostException {
        Set<String> ipAddresses = new HashSet<>();
        for (String serverName : serverNames) {
            ipAddresses.add(getIpAddress(serverName));
        }
        return ipAddresses;
    }

    protected String getIpAddress(String serverName) throws UnknownHostException {
        return InetAddress.getByName(serverName).getHostAddress();
    }

    private class VlanId {

        private int vlanId;

        public VlanId(int vlanId) {
            this.vlanId = vlanId;
        }
    }
}
