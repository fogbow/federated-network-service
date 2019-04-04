package cloud.fogbow.fns.core.intercomponent.xmpp.handlers;

import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class RemoteConfigureMemberRequestHandler extends AbstractQueryHandler {
    public RemoteConfigureMemberRequestHandler() {
        super(RemoteMethod.REMOTE_CONFIGURE_MEMBER.toString());
    }

    @Override
    public IQ handle(IQ iq) {
        return null;
    }

}
