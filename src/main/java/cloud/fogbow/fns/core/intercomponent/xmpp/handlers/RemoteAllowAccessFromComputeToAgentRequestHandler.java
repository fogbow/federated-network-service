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

public class RemoteAllowAccessFromComputeToAgentRequestHandler extends AbstractQueryHandler {
    public RemoteAllowAccessFromComputeToAgentRequestHandler() {
        super(RemoteMethod.REMOTE_ADD_INSTANCE_PUBLIC_KEY.toString());
    }

    @Override
    public IQ handle(IQ iq) {
        String publicKey = unmarshalPublicKey(iq);
        IQ response = iq.createResultIQ(iq);

        try {
            boolean result = RemoteFacade.getInstance().addInstancePublicKey(publicKey);
            System.out.println("added key successfully ++++++++++ " + result);
        } catch (UnexpectedException e) {
            XmppExceptionToErrorConditionTranslator.updateErrorCondition(response, e);
        }

        return response;
    }

    private String unmarshalPublicKey(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element hostIpElement = queryElement.element(IqElement.INSTANCE_PUBLIC_KEY.toString());
        return hostIpElement.getText();
    }
}
