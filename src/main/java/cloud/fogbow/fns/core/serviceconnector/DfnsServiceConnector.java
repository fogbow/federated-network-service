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
import cloud.fogbow.vlanid.api.http.response.VlanId;
import org.apache.log4j.Logger;

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

    public static final int SUCCESS_EXIT_CODE = 0;

    protected BashScriptRunner runner;

    public static final String SCRIPT_TARGET_PATH = "/tmp/";

    public DfnsServiceConnector() {
    }

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
