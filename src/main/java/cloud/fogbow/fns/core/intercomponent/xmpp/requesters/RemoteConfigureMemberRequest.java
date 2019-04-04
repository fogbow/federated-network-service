package cloud.fogbow.fns.core.intercomponent.xmpp.requesters;

import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import cloud.fogbow.ras.core.intercomponent.xmpp.requesters.RemoteRequest;

import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.intercomponent.xmpp.PacketSenderHolder;

import com.google.gson.Gson;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteConfigureMemberRequest implements RemoteRequest<MemberConfigurationState> {
    private String targetMember;
    private FederatedNetworkOrder order;

    public RemoteConfigureMemberRequest(String targetMember, FederatedNetworkOrder order) {
        this.targetMember = targetMember;
        this.order = order;
    }

    @Override
    public MemberConfigurationState send() throws Exception {
        IQ iq = marshal(this.order);
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.targetMember);
        return MemberConfigurationState.FAILED;
    }

    private IQ marshal(FederatedNetworkOrder order) {
        IQ iq = new IQ(IQ.Type.set);
        iq.setTo(this.targetMember);
        iq.setID(order.getId());

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(),
                RemoteMethod.REMOTE_CONFIGURE_MEMBER.toString());

        Element orderElement = queryElement.addElement(IqElement.FEDERATED_NETWORK_ORDER.toString());

        String orderJson = new Gson().toJson(order);
        orderElement.setText(orderJson);

        return iq;
    }
}
