package cloud.fogbow.fns.core.drivers.intercomponent.xmpp.requesters;

import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.IqElement;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import cloud.fogbow.ras.core.intercomponent.xmpp.requesters.RemoteRequest;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteRemoveAgentToComputeTunnelRequest implements RemoteRequest<Void> {
    private static final Logger LOGGER = Logger.getLogger(RemoteRemoveAgentToComputeTunnelRequest.class);

    private final String provider;
    private final FederatedNetworkOrder order;
    private final String hostIp;

    public RemoteRemoveAgentToComputeTunnelRequest(String provider, FederatedNetworkOrder order, String hostIp) {
        this.provider = provider;
        this.order = order;
        this.hostIp = hostIp;
    }

    @Override
    public Void send() throws Exception {
        IQ iq = marshal(this.provider, this.order, this.hostIp);
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.provider);

        return null;
    }

    public IQ marshal(String providerId, FederatedNetworkOrder order, String hostIp) {
        IQ iq = new IQ(IQ.Type.set);
        iq.setTo(SystemConstants.JID_SERVICE_NAME + SystemConstants.JID_CONNECTOR + SystemConstants.XMPP_SERVER_NAME_PREFIX + this.provider);

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(), RemoteMethod.REMOTE_REMOVE_AGENT_TO_COMPUTE_TUNNEL.toString());
        Element providerIdElement = queryElement.addElement(IqElement.PROVIDER_ID.toString());
        providerIdElement.setText(providerId);
        Element orderElement = queryElement.addElement(IqElement.FEDERATED_NETWORK_ORDER.toString());
        Element hostIpElement = queryElement.addElement(IqElement.HOST_IP.toString());
        hostIpElement.setText(hostIp);

        String orderJson = GsonHolder.getInstance().toJson(order);
        orderElement.setText(orderJson);

        return iq;
    }
}
