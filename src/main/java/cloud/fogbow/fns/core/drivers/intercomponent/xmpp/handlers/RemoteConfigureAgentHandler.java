package cloud.fogbow.fns.core.drivers.intercomponent.xmpp.handlers;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.core.drivers.intercomponent.RemoteFacade;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.drivers.dfns.AgentConfiguration;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppExceptionToErrorConditionTranslator;
import com.google.gson.Gson;
import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class RemoteConfigureAgentHandler extends AbstractQueryHandler {

    public RemoteConfigureAgentHandler() {
        super(RemoteMethod.REMOTE_CONFIGURE_AGENT.toString());
    }

    @Override
    public IQ handle(IQ iq) {
        String publicKey = unmarshalPublicKey(iq);
        String serviceName = unmarshalServiceName(iq);
        IQ response = iq.createResultIQ(iq);

        AgentConfiguration configuration;
        try {
            configuration = RemoteFacade.getInstance().configureAgent(publicKey, serviceName);
            marshalConfiguration(response, configuration);
        } catch (FogbowException e) {
            XmppExceptionToErrorConditionTranslator.updateErrorCondition(response, e);
        }

        return response;
    }

    private void marshalConfiguration(IQ response, AgentConfiguration configuration) {
        Element queryElement = response.getElement().addElement(IqElement.QUERY.toString(), RemoteMethod.REMOTE_CONFIGURE_AGENT.toString());
        Element agentConfiguration = queryElement.addElement(IqElement.REMOTE_AGENT_CONFIGURATION.toString());
        agentConfiguration.setText(new Gson().toJson(configuration));
    }

    private String unmarshalPublicKey(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element keyElement = queryElement.element(IqElement.INSTANCE_PUBLIC_KEY.toString());
        return keyElement.getText();
    }

    private String unmarshalServiceName(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element serviceNameElement = queryElement.element(IqElement.SERVICE_NAME.toString());
        return serviceNameElement.getText();
    }

}
