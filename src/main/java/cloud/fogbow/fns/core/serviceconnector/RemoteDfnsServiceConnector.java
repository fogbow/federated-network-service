package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.BashScriptRunner;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.*;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;;
import org.apache.log4j.Logger;

public class RemoteDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(RemoteDfnsServiceConnector.class);

    private String memberToBeConfigured;

    public RemoteDfnsServiceConnector(String memberToBeConfigured) {
        super(new BashScriptRunner());
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

    public void removeAgentToComputeTunnel(FederatedNetworkOrder order, String hostIp) throws UnexpectedException {
        RemoteRemoveAgentToComputeTunnelRequest request = new RemoteRemoveAgentToComputeTunnelRequest(
                this.memberToBeConfigured, order, hostIp);
        try {
            request.send();
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    @Override
    public void addKeyToAgentAuthorizedPublicKeys(String publicKey) throws UnexpectedException {
        RemoteAllowAccessFromComputeToAgentRequest request = new RemoteAllowAccessFromComputeToAgentRequest(this.memberToBeConfigured, publicKey);
        try {
            request.send();
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    public AgentConfiguration configureAgent(String publicKey) throws FogbowException {
        RemoteConfigureAgent remoteConfigureAgentRequest = new RemoteConfigureAgent(this.memberToBeConfigured, publicKey);
        try {
            return remoteConfigureAgentRequest.send();
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    @Override
    public SSAgentConfiguration getDfnsAgentConfiguration() throws UnexpectedException {
        RemoteGetDfnsAgentConfigurationRequest request = new RemoteGetDfnsAgentConfigurationRequest(this.memberToBeConfigured);

        try {
            SSAgentConfiguration dfnsAgentConfiguration = request.send();
            return dfnsAgentConfiguration;
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }
}
