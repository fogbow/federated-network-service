package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.requesters.*;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
;
import org.apache.log4j.Logger;

public class DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DfnsServiceConnector.class);

    private String memberToBeConfigured;

    public DfnsServiceConnector(String memberToBeConfigured) {
        this.memberToBeConfigured = memberToBeConfigured;
    }

    public void removeAgentToComputeTunnel(FederatedNetworkOrder order, String hostIp) throws UnexpectedException {
        RemoteRemoveAgentToComputeTunnelRequest request = new RemoteRemoveAgentToComputeTunnelRequest(
                this.memberToBeConfigured, order, hostIp);
        try {
            request.send();
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    public AgentConfiguration configureAgent(String publicKey, String serviceName) throws FogbowException {
        RemoteConfigureAgent remoteConfigureAgentRequest = new RemoteConfigureAgent(this.memberToBeConfigured, publicKey, serviceName);
        try {
            return remoteConfigureAgentRequest.send();
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }
}
