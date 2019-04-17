package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.InvalidParameterException;
import cloud.fogbow.common.exceptions.NoAvailableResourcesException;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteConfigureMemberRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteGetVlanIdRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteReleaseVlanIdRequest;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;

public class RemoteDfnsServiceConnector extends DfnsServiceConnector {
    private String memberToBeConfigured;

    public RemoteDfnsServiceConnector(String memberToBeConfigured) {
        this.memberToBeConfigured = memberToBeConfigured;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        int vlanId = -1;

        try {
            RemoteGetVlanIdRequest remoteGetVlanIdRequest = new RemoteGetVlanIdRequest(memberToBeConfigured);
            vlanId = remoteGetVlanIdRequest.send();
        } catch (Exception e) {
            if (e instanceof NoAvailableResourcesException) {
                throw new NoVlanIdsLeftException();
            }

            e.printStackTrace();
        }

        return vlanId;
    }

    @Override
    public void releaseVlanId(int vlanId) {
        RemoteReleaseVlanIdRequest remoteGetVlanIdRequest = new RemoteReleaseVlanIdRequest(memberToBeConfigured, vlanId);

        try {
            remoteGetVlanIdRequest.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
