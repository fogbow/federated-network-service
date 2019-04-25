package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.NoAvailableResourcesException;
import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteGetVlanIdRequest;
import cloud.fogbow.fns.core.intercomponent.xmpp.requesters.RemoteReleaseVlanIdRequest;
import org.apache.log4j.Logger;

public abstract class DfnsServiceConnector implements ServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(DfnsServiceConnector.class);

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

            LOGGER.error(e.getMessage(), e);
        }

        return vlanId;
    }

    @Override
    public boolean releaseVlanId(int vlanId) {
        String xmppVlanIdServiceJid = PropertiesHolder.getInstance()
                .getProperty(ConfigurationPropertyDefaults.XMPP_VLAN_ID_SERVICE_JID);
        RemoteReleaseVlanIdRequest remoteGetVlanIdRequest = new RemoteReleaseVlanIdRequest(xmppVlanIdServiceJid, vlanId);

        try {
            remoteGetVlanIdRequest.send();
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }
}
