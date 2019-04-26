package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.core.intercomponent.xmpp.handlers.RemoteAddInstancePublicKeyRequestHandler;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteAddInstancePublicKeyRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteConfigureMemberRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteRemoveAgentToComputeTunnelRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteRemoveFedNetRequest;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import org.apache.log4j.Logger;

public class RemoteDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(RemoteDfnsServiceConnector.class);

    private String memberToBeConfigured;

    public RemoteDfnsServiceConnector(String memberToBeConfigured) {
        this.memberToBeConfigured = memberToBeConfigured;
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        RemoteConfigureMemberRequest request = new RemoteConfigureMemberRequest(this.memberToBeConfigured, order);
        try {
            MemberConfigurationState state = request.send();
            return state;
        } catch (Exception e) {
            return MemberConfigurationState.FAILED;
        }
    }

    @Override
    public boolean remove(FederatedNetworkOrder order) {
        RemoteRemoveFedNetRequest remoteRemoveFedNetRequest = new RemoteRemoveFedNetRequest(memberToBeConfigured, order);
        try {
            remoteRemoveFedNetRequest.send();
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean removeAgentToComputeTunnel(String hostIp, int vlanId) throws UnexpectedException {
        RemoteRemoveAgentToComputeTunnelRequest request = new RemoteRemoveAgentToComputeTunnelRequest(this.memberToBeConfigured, hostIp, vlanId);
        try {
            request.send();
            return true;
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    @Override
    public boolean addInstancePublicKeyToAgent(String instancePublicKey) throws UnexpectedException {
        RemoteAddInstancePublicKeyRequest request = new RemoteAddInstancePublicKeyRequest(this.memberToBeConfigured, instancePublicKey);
        try {
            request.send();
            return true;
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }
}
