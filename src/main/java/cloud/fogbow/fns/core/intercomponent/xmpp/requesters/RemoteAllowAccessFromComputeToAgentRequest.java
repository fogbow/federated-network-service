package cloud.fogbow.fns.core.intercomponent.xmpp.requesters;

import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import cloud.fogbow.ras.core.intercomponent.xmpp.requesters.RemoteRequest;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteAllowAccessFromComputeToAgentRequest implements RemoteRequest<Void> {
    private static final Logger LOGGER = Logger.getLogger(RemoteAllowAccessFromComputeToAgentRequest.class);

    private final String provider;
    private final String instancePublicKey;

    public RemoteAllowAccessFromComputeToAgentRequest(String provider, String instancePublicKey) {
        this.provider = provider;
        this.instancePublicKey = instancePublicKey;
    }

    @Override
    public Void send() throws Exception {
        IQ iq = marshal(this.instancePublicKey);
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.provider);

        return null;
    }

    public IQ marshal(String instancePublicKey) {
        IQ iq = new IQ(IQ.Type.set);
        iq.setTo(this.provider);

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(), RemoteMethod.REMOTE_ADD_INSTANCE_PUBLIC_KEY.toString());
        queryElement.addElement(IqElement.INSTANCE_PUBLIC_KEY.toString(), instancePublicKey);

        return iq;
    }
}
