package cloud.fogbow.fns.core.intercomponent.xmpp.handlers;

import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.core.intercomponent.RemoteFacade;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.serviceconnector.DfnsAgentConfiguration;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppExceptionToErrorConditionTranslator;
import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class RemoteGetDfnsAgentConfigurationRequestHandler extends AbstractQueryHandler {
    private static final String REMOTE_GET_DFNS_AGENT_CONFIGURATION = RemoteMethod.REMOTE_GET_DFNS_AGENT_CONFIGURATION.toString();

    public RemoteGetDfnsAgentConfigurationRequestHandler() {
        super(REMOTE_GET_DFNS_AGENT_CONFIGURATION);
    }

    @Override
    public IQ handle(IQ iq) {
        IQ response = IQ.createResultIQ(iq);
        System.out.println("received remote message get dfns configuration ++++++++" + iq.toXML());

        try {
            DfnsAgentConfiguration dfnsAgentConfiguration = RemoteFacade.getInstance().getDfnsAgentConfiguration();
            updateResponse(response, dfnsAgentConfiguration);
        } catch (Exception e) {
            XmppExceptionToErrorConditionTranslator.updateErrorCondition(response, e);
        }

        System.out.println("sending response get dfns configuration ++++++++" + response.toXML());

        return response;
    }

    private void updateResponse(IQ response, DfnsAgentConfiguration dfnsAgentConfiguration) {
        Element queryElement = response.getElement().addElement(IqElement.QUERY.toString(), REMOTE_GET_DFNS_AGENT_CONFIGURATION);
        Element dfnsAgentConfigurationElement = queryElement.addElement(IqElement.DFNS_AGENT_CONFIGURATION.toString());
        Element dfnsAgentConfigurationClassElement = queryElement.addElement(IqElement.DFNS_AGENT_CONFIGURATION_CLASS.toString());

        dfnsAgentConfigurationClassElement.setText(dfnsAgentConfiguration.getClass().getName());
        dfnsAgentConfigurationElement.setText(GsonHolder.getInstance().toJson(dfnsAgentConfiguration));
    }
}
