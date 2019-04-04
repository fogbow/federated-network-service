package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

// TODO ARNETT implement
public class RemoteDfnsServiceConnector extends DfnsServiceConnector {
    private String memberToBeConfigured;

    public RemoteDfnsServiceConnector(String memberToBeConfigured) {
        this.memberToBeConfigured = memberToBeConfigured;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        return -1;
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        return MemberConfigurationState.FAILED;
    }
}
