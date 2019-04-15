package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteConfigureMemberRequest;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

public class RemoteDfnsServiceConnector extends DfnsServiceConnector {
    private String memberToBeConfigured;

    public RemoteDfnsServiceConnector(String memberToBeConfigured) {
        this.memberToBeConfigured = memberToBeConfigured;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        // TODO DFNS
        return -1;
    }

    @Override
    public void releaseVlanId() {
        // TODO DFNS
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        RemoteConfigureMemberRequest request = new RemoteConfigureMemberRequest(this.memberToBeConfigured, order);
        try {
            return request.send();
        } catch (Exception e) {
            return MemberConfigurationState.FAILED;
        }
    }
}
