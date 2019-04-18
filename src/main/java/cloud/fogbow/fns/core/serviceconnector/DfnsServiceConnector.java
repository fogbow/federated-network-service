package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.NoAvailableResourcesException;
import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteGetVlanIdRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteReleaseVlanIdRequest;

public abstract class DfnsServiceConnector implements ServiceConnector {

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        int vlanId = -1;

        try {
            String xmppVlanIdServiceJid = PropertiesHolder.getInstance()
                    .getProperty(ConfigurationPropertyDefaults.XMPP_VLAN_ID_SERVICE_JID);
            RemoteGetVlanIdRequest remoteGetVlanIdRequest = new RemoteGetVlanIdRequest(xmppVlanIdServiceJid);
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
        String xmppVlanIdServiceJid = PropertiesHolder.getInstance()
                .getProperty(ConfigurationPropertyDefaults.XMPP_VLAN_ID_SERVICE_JID);
        RemoteReleaseVlanIdRequest remoteGetVlanIdRequest = new RemoteReleaseVlanIdRequest(xmppVlanIdServiceJid, vlanId);

        try {
            remoteGetVlanIdRequest.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
