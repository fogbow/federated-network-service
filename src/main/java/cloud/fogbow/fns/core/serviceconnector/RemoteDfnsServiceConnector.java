package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteConfigureMemberRequest;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

public class RemoteDfnsServiceConnector extends DfnsServiceConnector {
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
}
