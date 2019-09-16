package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.constants.HttpConstants;
import cloud.fogbow.common.constants.HttpMethod;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.BashScriptRunner;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.common.util.connectivity.HttpRequestClient;
import cloud.fogbow.common.util.connectivity.HttpResponse;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;

public abstract class DfnsServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DfnsServiceConnector.class);

    protected BashScriptRunner runner;

    public static final String VLAN_ID_SERVICE_URL = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.VLAN_ID_SERVICE_URL_KEY);
    public static final String VLAN_ID_ENDPOINT = "/vlanId";
    public static final int PUBLIC_KEY_INDEX = 0;
    public static final int PRIVATE_KEY_INDEX = 1;
    private final String GEN_KEY_PAIR_SCRIPT_PATH = Paths.get("").toAbsolutePath().toString() + "/bin/agent-scripts/generateSshKeyPair";
    private final String KEY_PAIR_SEPARATOR = "KEY SEPARATOR";

    public DfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public int acquireVlanId() throws FogbowException {
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

    @Override
    public boolean releaseVlanId(int vlanId) throws FogbowException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.CONTENT_TYPE_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);
        headers.put(HttpConstants.ACCEPT_KEY, HttpConstants.JSON_CONTENT_TYPE_KEY);

        String jsonBody = GsonHolder.getInstance().toJson(new VlanId(vlanId));
        HashMap<String, String> body = GsonHolder.getInstance().fromJson(jsonBody, HashMap.class);

        String releaseVlanIdEndpoint = VLAN_ID_SERVICE_URL + VLAN_ID_ENDPOINT;

        HttpResponse response = HttpRequestClient.doGenericRequest(HttpMethod.POST, releaseVlanIdEndpoint, headers, body);

        if (response.getHttpCode() == HttpStatus.NOT_FOUND.value()) {
            LOGGER.warn(String.format(Messages.Warn.UNABLE_TO_RELEASE_VLAN_ID, vlanId));
            return true;
        }

        return response.getHttpCode() == HttpStatus.OK.value();
    }

    @Override
    public final UserData getTunnelCreationInitScript(String federatedIp, FederatedCompute compute, FederatedNetworkOrder order) throws UnexpectedException {
        try {
            String[] keys = generateSshKeyPair();
            addKeyToAgentAuthorizedPublicKeys(keys[PUBLIC_KEY_INDEX]);

            DfnsAgentConfiguration dfnsAgentConfiguration = getDfnsAgentConfiguration();
            dfnsAgentConfiguration.setPublicKey(keys[PUBLIC_KEY_INDEX]);

            String privateIpAddress = dfnsAgentConfiguration.getPrivateIpAddress();
            return FederatedComputeUtil.getDfnsUserData(dfnsAgentConfiguration, federatedIp, privateIpAddress,
                   order.getVlanId(), keys[PRIVATE_KEY_INDEX]);
        } catch (IOException | GeneralSecurityException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    protected String[] generateSshKeyPair() throws UnexpectedException {
        BashScriptRunner runner = new BashScriptRunner();
        String keyName = String.valueOf(UUID.randomUUID());

        String[] genCommand = {"bash", GEN_KEY_PAIR_SCRIPT_PATH, keyName};
        BashScriptRunner.Output createCommandResult = runner.runtimeRun(genCommand);

        return new String[]{createCommandResult.getContent().split(KEY_PAIR_SEPARATOR)[0], createCommandResult.getContent().split(KEY_PAIR_SEPARATOR)[1]};
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
    public abstract DfnsAgentConfiguration getDfnsAgentConfiguration() throws UnknownHostException, UnexpectedException;

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
