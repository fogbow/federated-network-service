package cloud.fogbow.fns.core.intercomponent.xmpp.handlers;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.core.intercomponent.RemoteFacade;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppExceptionToErrorConditionTranslator;
import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class RemoteRemoveFedNetRequestHandler extends AbstractQueryHandler {
    public RemoteRemoveFedNetRequestHandler() {
        super(RemoteMethod.REMOTE_REMOVE_FEDNET.toString());
    }

    @Override
    public IQ handle(IQ iq) {
        FederatedNetworkOrder order = unmarshalOrder(iq);
        IQ response = iq.createResultIQ(iq);

        try {
            RemoteFacade.getInstance().remove(order);
        } catch (UnexpectedException e) {
            XmppExceptionToErrorConditionTranslator.updateErrorCondition(response, e);
        }

        return response;
    }

    private FederatedNetworkOrder unmarshalOrder(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element fedNetOrderElement = queryElement.element(IqElement.FEDERATED_NETWORK_ORDER.toString());
        String fedNetOrderElementText = fedNetOrderElement.getText();

        return GsonHolder.getInstance().fromJson(fedNetOrderElementText, FederatedNetworkOrder.class);
    }
}
