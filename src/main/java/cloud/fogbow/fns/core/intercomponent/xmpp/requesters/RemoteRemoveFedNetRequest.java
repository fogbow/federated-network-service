package cloud.fogbow.fns.core.intercomponent.xmpp.requesters;

import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import cloud.fogbow.ras.core.intercomponent.xmpp.requesters.RemoteRequest;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteRemoveFedNetRequest implements RemoteRequest<Void> {
    private static final Logger LOGGER = Logger.getLogger(RemoteRemoveFedNetRequest.class);

    private String targetMember;
    private FederatedNetworkOrder order;

    public RemoteRemoveFedNetRequest(String targetMember, FederatedNetworkOrder order) {
        this.targetMember = targetMember;
        this.order = order;
    }

    @Override
    public Void send() throws Exception {
        IQ iq = RemoteRemoveFedNetRequest.marshal(this.targetMember, this.order);
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.targetMember);

        return null;
    }

    public static IQ marshal(String targetMember, FederatedNetworkOrder order) {
        IQ iq = new IQ(IQ.Type.set);
        iq.setTo(targetMember);
        iq.setID(order.getId());

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(), RemoteMethod.REMOTE_REMOVE_FEDNET.toString());
        Element orderElement = queryElement.addElement(IqElement.FEDERATED_NETWORK_ORDER.toString());

        String orderJson = GsonHolder.getInstance().toJson(order);
        orderElement.setText(orderJson);

        return iq;
    }
}
