package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.common.util.HttpErrorToFogbowExceptionMapper;
import cloud.fogbow.fns.api.parameters.Compute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.BashScriptRunner;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.fns.utils.RedirectUtil;
import cloud.fogbow.ras.api.http.ExceptionResponse;
import cloud.fogbow.ras.core.models.UserData;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class DfnsServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DfnsServiceConnector.class);

    private static final int SUCCESS_EXIT_CODE = 0;
    public static final String TUNNEL_SCRIPT_REMOTE_PATH = "/tmp";
    public static final String CREATE_TUNNEL_SCRIPT_PATH = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.CREATE_TUNNEL_FROM_AGENT_TO_COMPUTE_SCRIPT_PATH);

    private Gson gson = new Gson();
    protected BashScriptRunner runner;

    public DfnsServiceConnector() {
    }

    public DfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException, FogbowException {
        ResponseEntity<String> responseEntity = null;

        // We need a try-catch here, because a connect exception may be thrown
        try {
            responseEntity = RedirectUtil.createAndSendRequest("/" + VlanId.VLAN_ID_ENDPOINT, null,
                    HttpMethod.GET, null, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.VLAN_ID_SERVICE_DOES_NOT_RESPOND);
        }

        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
        }

        VlanId vlanId = gson.fromJson(responseEntity.getBody(), VlanId.class);

        return vlanId.getVlanId();
    }

    @Override
    public boolean releaseVlanId(int vlanId) throws FogbowException {
        ResponseEntity<String> responseEntity = null;

        // We need a try-catch here, because a connect exception may be thrown
        try {
            String body = gson.toJson(new VlanId(vlanId));
            responseEntity = RedirectUtil.createAndSendRequest("/" + VlanId.VLAN_ID_ENDPOINT, body,
                    HttpMethod.POST, null, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.VLAN_ID_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
        }

        VlanId vlanId1 = gson.fromJson(responseEntity.getBody(), VlanId.class);

        return vlanId1.getVlanId();
    }

    @Override
    public UserData getTunnelCreationInitScript(String federatedIp, Compute compute, FederatedNetworkOrder order) throws UnexpectedException {
        try {
            KeyPair keyPair = CryptoUtil.generateKeyPair();

            addKeyToAgentAuthorizedPublicKeys(serializePublicKey(keyPair.getPublic()));
            copyCreateTunnelFromAgentToComputeScript();

            DfnsAgentConfiguration dfnsAgentConfiguration = getDfnsAgentConfiguration(CryptoUtil.savePublicKey(keyPair.getPublic()));
            String agentIp = getIpAddress(compute.getCompute().getProvider());
            return FederatedComputeUtil.getDfnsUserData(dfnsAgentConfiguration, federatedIp, agentIp, order.getVlanId(), keyPair.getPrivate());
        } catch (IOException | GeneralSecurityException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    private boolean copyCreateTunnelFromAgentToComputeScript() throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
        String sshCredentials = agentUser + "@" + agentPublicIp;
        String scpPath = sshCredentials + ":" + TUNNEL_SCRIPT_REMOTE_PATH;

        BashScriptRunner.Output output = this.runner.run("scp", "-i", permissionFilePath, CREATE_TUNNEL_SCRIPT_PATH, scpPath);

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
}
