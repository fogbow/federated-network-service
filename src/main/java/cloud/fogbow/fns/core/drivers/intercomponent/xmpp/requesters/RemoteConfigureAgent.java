package cloud.fogbow.fns.core.drivers.intercomponent.xmpp.requesters;

import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.drivers.dfns.AgentConfiguration;
import cloud.fogbow.fns.core.drivers.dfns.SSAgentConfiguration;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteConfigureAgent {

    private final String provider;
    private final String publicKey;
    private final String serviceName;

    public RemoteConfigureAgent(String provider, String publicKey, String serviceName) {
        this.provider = provider;
        this.publicKey = publicKey;
        this.serviceName = serviceName;
    }

    public AgentConfiguration send() throws Exception {
        IQ iq = marshal();
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.provider);
        return unmarshal(response);
    }

    private IQ marshal() {
        IQ iq = new IQ(IQ.Type.set);
        iq.setTo(SystemConstants.JID_SERVICE_NAME + SystemConstants.JID_CONNECTOR + SystemConstants.XMPP_SERVER_NAME_PREFIX + this.provider);

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(),
                RemoteMethod.REMOTE_CONFIGURE_AGENT.toString());

        Element instancePublicKeyElement = queryElement.addElement(IqElement.INSTANCE_PUBLIC_KEY.toString());
        instancePublicKeyElement.setText(publicKey);

        Element serviceNameElement = queryElement.addElement(IqElement.SERVICE_NAME.toString());
        serviceNameElement.setText(this.serviceName);

        return iq;
    }

    private AgentConfiguration unmarshal(IQ response) {
        Element queryElement = response.getElement().element(IqElement.QUERY.toString());
        Element agentConfiguration = queryElement.element(IqElement.REMOTE_AGENT_CONFIGURATION.toString());
        return GsonHolder.getInstance().fromJson(agentConfiguration.getText(), SSAgentConfiguration.class);
    }
}
