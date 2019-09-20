package cloud.fogbow.fns.core.intercomponent.xmpp;

import cloud.fogbow.fns.core.intercomponent.xmpp.handlers.*;
import org.apache.log4j.Logger;
import org.jamppa.component.XMPPComponent;

public class XmppComponentManager extends XMPPComponent {
    private static Logger LOGGER = Logger.getLogger(XmppComponentManager.class);

    public static final String XMPP_JID_PREFIX = "fns-";

    public XmppComponentManager(String jid, String password, String xmppServerIp, int xmppServerPort, long timeout) {
        super(jid, password, xmppServerIp, xmppServerPort, timeout);
        // instantiate set handlers here
        addSetHandler(new RemoteConfigureAgentHandler());
        addSetHandler(new RemoteRemoveAgentToComputeTunnelRequestHandler());
        // instantiate get handlers here
    }
}
