package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

// TODO ARNETT implement
public class LocalDfnsServiceConnector extends DfnsServiceConnector {
    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        return -1;
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        return MemberConfigurationState.FAILED;
    }
}
