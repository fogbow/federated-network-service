package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.constants.HttpConstants;
import cloud.fogbow.common.constants.HttpMethod;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.common.util.connectivity.HttpRequestClient;
import cloud.fogbow.common.util.connectivity.HttpResponse;
import cloud.fogbow.fns.api.parameters.Compute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.BashScriptRunner;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import com.google.gson.Gson;
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

    private Gson gson = new Gson();
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

        cloud.fogbow.vlanid.api.http.response.VlanId vlanId = gson.fromJson(response.getContent(),
                cloud.fogbow.vlanid.api.http.response.VlanId.class);

        return vlanId.getVlanId();
    }

    @Override
    public boolean releaseVlanId(int vlanId) throws FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);

        String jsonBody = GsonHolder.getInstance().toJson(new cloud.fogbow.vlanid.api.http.response.VlanId(vlanId));
        HashMap<String, String> body = GsonHolder.getInstance().fromJson(jsonBody, HashMap.class);

        String releaseVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.POST, releaseVlanIdEndpoint, headers, body);

        return response.getHttpCode() == HttpStatus.OK.value();
    }

    @Override
    public UserData getTunnelCreationInitScript(String federatedIp, Compute compute, FederatedNetworkOrder order) throws UnexpectedException {
        try {
            KeyPair keyPair = CryptoUtil.generateKeyPair();

            addKeyToAgentAuthorizedPublicKeys(serializePublicKey(keyPair.getPublic()));

            DfnsAgentConfiguration dfnsAgentConfiguration = getDfnsAgentConfiguration(CryptoUtil.savePublicKey(keyPair.getPublic()));
            String agentIp = getIpAddress(compute.getCompute().getProvider());
            return FederatedComputeUtil.getDfnsUserData(dfnsAgentConfiguration, federatedIp, agentIp, order.getVlanId(), keyPair.getPrivate());
        } catch (IOException | GeneralSecurityException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    /**
     * Sends the script that creates a tunnel from the agent to the compute to the agent
     * @return
     */
    public abstract boolean copyScriptForTunnelFromAgentToComputeCreationIntoAgent() throws UnexpectedException;

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
}
