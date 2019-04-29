package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.NoAvailableResourcesException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.fns.api.parameters.Compute;
import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteAcquireVlanIdRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteReleaseVlanIdRequest;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.BashScriptRunner;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public abstract class DfnsServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DfnsServiceConnector.class);

    private static final int SUCCESS_EXIT_CODE = 0;
    public static final String TUNNEL_SCRIPT_REMOTE_PATH = "/tmp";
    public static final String CREATE_TUNNEL_SCRIPT_PATH = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.CREATE_TUNNEL_FROM_AGENT_TO_COMPUTE_SCRIPT_PATH);

    private BashScriptRunner runner;

    public DfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        int vlanId = -1;

        try {
            String xmppVlanIdServiceJid = PropertiesHolder.getInstance()
                    .getProperty(ConfigurationPropertyDefaults.XMPP_VLAN_ID_SERVICE_JID);
            RemoteAcquireVlanIdRequest remoteAcquireVlanIdRequest = new RemoteAcquireVlanIdRequest(xmppVlanIdServiceJid);
            vlanId = remoteAcquireVlanIdRequest.send();
        } catch (Exception e) {
            if (e instanceof NoAvailableResourcesException) {
                throw new NoVlanIdsLeftException();
            }

            LOGGER.error(e.getMessage(), e);
        }

        return vlanId;
    }

    @Override
    public boolean releaseVlanId(int vlanId) {
        String xmppVlanIdServiceJid = PropertiesHolder.getInstance()
                .getProperty(ConfigurationPropertyDefaults.XMPP_VLAN_ID_SERVICE_JID);
        RemoteReleaseVlanIdRequest remoteGetVlanIdRequest = new RemoteReleaseVlanIdRequest(xmppVlanIdServiceJid, vlanId);

        try {
            remoteGetVlanIdRequest.send();
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public UserData getTunnelCreationInitScript(String federatedIp, Compute compute, FederatedNetworkOrder order) throws UnexpectedException {
        try {
            KeyPair keyPair = CryptoUtil.generateKeyPair();

            this.allowAccessFromComputeToAgent(keyPair.getPublic().toString());
            this.copyCreateTunnelFromAgentToComputeScript();

            return FederatedComputeUtil.getDfnsUserData(federatedIp, compute.getCompute().getProvider(), order.getVlanId(), keyPair.getPrivate());
        } catch (NoSuchAlgorithmException|IOException e) {
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

    public abstract boolean allowAccessFromComputeToAgent(String instancePublicKey) throws UnexpectedException;
}
