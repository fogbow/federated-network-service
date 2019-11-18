package cloud.fogbow.fns.core.drivers.intercomponent.xmpp.handlers;

import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.core.drivers.intercomponent.RemoteFacade;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppExceptionToErrorConditionTranslator;
import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class RemoteRemoveAgentToComputeTunnelRequestHandler extends AbstractQueryHandler {
    public RemoteRemoveAgentToComputeTunnelRequestHandler() {
        super(RemoteMethod.REMOTE_REMOVE_AGENT_TO_COMPUTE_TUNNEL.toString());
    }

    @Override
    public IQ handle(IQ iq) {
        String providerId = unmarshalProviderId(iq);
        FederatedNetworkOrder order = unmarshalOrder(iq);
        String hostIp = unmarshalHostIp(iq);
        IQ response = iq.createResultIQ(iq);

        try {
            RemoteFacade.getInstance().removeAgentToComputeTunnel(providerId, order, hostIp);
        } catch (Throwable e) {
            XmppExceptionToErrorConditionTranslator.updateErrorCondition(response, e);
        }

        return response;
    }

    private String unmarshalProviderId(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element providerIdElement = queryElement.element(IqElement.PROVIDER_ID.toString());
        return providerIdElement.getText();
    }

    private FederatedNetworkOrder unmarshalOrder(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element fedNetOrderElement = queryElement.element(IqElement.FEDERATED_NETWORK_ORDER.toString());
        String fedNetOrderElementText = fedNetOrderElement.getText();
        return GsonHolder.getInstance().fromJson(fedNetOrderElementText, FederatedNetworkOrder.class);
    }

    private String unmarshalHostIp(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element hostIpElement = queryElement.element(IqElement.HOST_IP.toString());
        return hostIpElement.getText();
    }
}
