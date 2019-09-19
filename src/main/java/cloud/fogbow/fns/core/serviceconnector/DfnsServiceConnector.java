package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.BashScriptRunner;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.*;

public abstract class DfnsServiceConnector extends DefaultServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DfnsServiceConnector.class);

    protected BashScriptRunner runner;

    public DfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

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
