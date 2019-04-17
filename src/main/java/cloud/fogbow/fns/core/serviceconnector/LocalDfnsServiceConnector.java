package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.NoAvailableResourcesException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteGetVlanIdRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteReleaseVlanIdRequest;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.BashScriptRunner;
import org.apache.log4j.Logger;

public class LocalDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(LocalDfnsServiceConnector.class);
    private static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.XMPP_JID_KEY);

    private BashScriptRunner runner;

    public LocalDfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        int vlanId = -1;

        try {
            RemoteGetVlanIdRequest remoteGetVlanIdRequest = new RemoteGetVlanIdRequest(LOCAL_MEMBER_NAME);
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
        RemoteReleaseVlanIdRequest remoteGetVlanIdRequest = new RemoteReleaseVlanIdRequest(LOCAL_MEMBER_NAME, vlanId);

        try {
            remoteGetVlanIdRequest.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        try {
            this.runner.run("echo", "Hello");
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }

        return MemberConfigurationState.FAILED;
    }
}
