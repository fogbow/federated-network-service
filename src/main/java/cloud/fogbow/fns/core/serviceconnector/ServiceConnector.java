package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

public interface ServiceConnector {
    int acquireVlanId() throws NoVlanIdsLeftException;

    void releaseVlanId(int vlanId);

    MemberConfigurationState configure(FederatedNetworkOrder order);
}
