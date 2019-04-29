package cloud.fogbow.fns.core.intercomponent.xmpp;

import cloud.fogbow.fns.core.intercomponent.xmpp.handlers.RemoteAllowAccessFromComputeToAgentRequestHandler;
import cloud.fogbow.fns.core.intercomponent.xmpp.handlers.RemoteConfigureMemberRequestHandler;
import cloud.fogbow.fns.core.intercomponent.xmpp.handlers.RemoteRemoveAgentToComputeTunnelRequestHandler;
import cloud.fogbow.fns.core.intercomponent.xmpp.handlers.RemoteRemoveFedNetRequestHandler;
import org.apache.log4j.Logger;
import org.jamppa.component.XMPPComponent;

public class XmppComponentManager extends XMPPComponent {
    private static Logger LOGGER = Logger.getLogger(XmppComponentManager.class);

    public XmppComponentManager(String jid, String password, String xmppServerIp, int xmppServerPort, long timeout) {
        super(jid, password, xmppServerIp, xmppServerPort, timeout);
        // instantiate set handlers here
        addSetHandler(new RemoteConfigureMemberRequestHandler());
        addSetHandler(new RemoteRemoveFedNetRequestHandler());
        addSetHandler(new RemoteRemoveAgentToComputeTunnelRequestHandler());
        addSetHandler(new RemoteRemoveAgentToComputeTunnelRequestHandler());
        addSetHandler(new RemoteAllowAccessFromComputeToAgentRequestHandler());
        // instantiate get handlers here
    }
}
