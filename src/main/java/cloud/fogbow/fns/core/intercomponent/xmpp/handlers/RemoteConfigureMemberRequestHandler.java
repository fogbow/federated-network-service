package cloud.fogbow.fns.core.intercomponent.xmpp.handlers;

import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.core.intercomponent.RemoteFacade;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import com.google.gson.Gson;
import org.dom4j.Element;
import org.jamppa.component.handler.AbstractQueryHandler;
import org.xmpp.packet.IQ;

public class RemoteConfigureMemberRequestHandler extends AbstractQueryHandler {
    public RemoteConfigureMemberRequestHandler() {
        super(RemoteMethod.REMOTE_CONFIGURE_MEMBER.toString());
    }

    @Override
    public IQ handle(IQ iq) {
        FederatedNetworkOrder order = unmarshalOrder(iq);
        MemberConfigurationState state = configureMember(order);

        IQ response = iq.createResultIQ(iq);
        Element queryElement = response.getElement().addElement(IqElement.QUERY.toString(), RemoteMethod.REMOTE_CONFIGURE_MEMBER.toString());
        Element memberConfigurationState = queryElement.addElement(IqElement.MEMBER_CONFIGURATION_STATE.toString());
        memberConfigurationState.setText(new Gson().toJson(state));
        return response;
    }

    private FederatedNetworkOrder unmarshalOrder(IQ iq) {
        Element queryElement = iq.getElement().element(IqElement.QUERY.toString());
        Element fedNetOrderElement = queryElement.element(IqElement.FEDERATED_NETWORK_ORDER.toString());
        String fedNetOrderElementText = fedNetOrderElement.getText();
        return GsonHolder.getInstance().fromJson(fedNetOrderElementText, FederatedNetworkOrder.class);
    }

    private MemberConfigurationState configureMember(FederatedNetworkOrder order) {
        return RemoteFacade.getInstance().configureMember(order);
    }
}
