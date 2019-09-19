package cloud.fogbow.fns.core.intercomponent.xmpp.requesters;

import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.serviceconnector.AgentConfiguration;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteConfigureAgent {

    private final String provider;
    private final String publicKey;

    public RemoteConfigureAgent(String provider, String publicKey) {
        this.provider = provider;
        this.publicKey = publicKey;
    }

    public AgentConfiguration send() throws Exception {
        IQ iq = marshal();
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.provider);
        return unmarshal(response);
    }

    private IQ marshal() {
        IQ iq = new IQ(IQ.Type.set);
        iq.setTo(this.provider);

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(),
                RemoteMethod.REMOTE_CONFIGURE_AGENT.toString());

        Element instancePublicKeyElement = queryElement.addElement(IqElement.INSTANCE_PUBLIC_KEY.toString());
        instancePublicKeyElement.setText(publicKey);

        return iq;
    }

    private AgentConfiguration unmarshal(IQ response) {
        Element queryElement = response.getElement().element(IqElement.QUERY.toString());
        Element agentConfiguration = queryElement.element(IqElement.DFNS_AGENT_CONFIGURATION.toString());
        return GsonHolder.getInstance().fromJson(agentConfiguration.getText(), AgentConfiguration.class);
    }
}
