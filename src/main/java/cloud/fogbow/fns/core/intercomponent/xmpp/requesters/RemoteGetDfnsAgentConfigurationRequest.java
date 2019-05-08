package cloud.fogbow.fns.core.intercomponent.xmpp.requesters;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.core.serviceconnector.DfnsAgentConfiguration;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import cloud.fogbow.ras.core.intercomponent.xmpp.requesters.RemoteRequest;
import com.google.gson.Gson;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteGetDfnsAgentConfigurationRequest implements RemoteRequest<DfnsAgentConfiguration> {
    private String provider;
    private String publicKey;

    public RemoteGetDfnsAgentConfigurationRequest(String provider, String publicKey) {
        this.provider = provider;
        this.publicKey = publicKey;
    }

    @Override
    public DfnsAgentConfiguration send() throws Exception {
        IQ iq = marshal(this.provider, this.publicKey);
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.provider);
        DfnsAgentConfiguration dfnsAgentConfiguration = unmarshalInstance(response);

        return dfnsAgentConfiguration;
    }

    public static IQ marshal(String provider, String publicKey) {
        IQ iq = new IQ(IQ.Type.get);
        iq.setTo(provider);

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(),
                RemoteMethod.REMOTE_GET_DFNS_AGENT_CONFIGURATION.toString());

        Element dfnsAgentConfigurationElement = queryElement.addElement(IqElement.INSTANCE_PUBLIC_KEY.toString());
        dfnsAgentConfigurationElement.setText(publicKey);

        return iq;
    }

    private DfnsAgentConfiguration unmarshalInstance(IQ response) throws UnexpectedException {

        Element queryElement = response.getElement().element(IqElement.QUERY.toString());
        String instanceStr = queryElement.element(IqElement.DFNS_AGENT_CONFIGURATION.toString()).getText();

        String instanceClassName = queryElement.element(IqElement.DFNS_AGENT_CONFIGURATION_CLASS.toString()).getText();

        DfnsAgentConfiguration dfnsAgentConfiguration = null;
        try {
            dfnsAgentConfiguration = (DfnsAgentConfiguration) new Gson().fromJson(instanceStr, Class.forName(instanceClassName));
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }

        return dfnsAgentConfiguration;
    }
}
