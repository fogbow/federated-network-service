package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

public abstract class DfnsServiceConnector implements ServiceConnector {
    @Override
    public abstract int acquireVlanId() throws NoVlanIdsLeftException;

    @Override
    public abstract MemberConfigurationState configure(FederatedNetworkOrder order);
}
