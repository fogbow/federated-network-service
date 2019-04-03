package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

public class DfnsServiceConnector implements ServiceConnector {
    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        // TODO ARNETT Connect to the Master VlanId Server and acquire a vlan id from it
        return 0;
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        // TODO ARNETT
        return MemberConfigurationState.FAILED;
    }
}
