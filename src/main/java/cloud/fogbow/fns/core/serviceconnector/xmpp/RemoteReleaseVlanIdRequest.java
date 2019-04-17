package cloud.fogbow.fns.core.serviceconnector.xmpp;

import cloud.fogbow.ras.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.ras.core.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import cloud.fogbow.ras.core.intercomponent.xmpp.requesters.RemoteRequest;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

public class RemoteReleaseVlanIdRequest implements RemoteRequest<Void> {
    private static final Logger LOGGER = Logger.getLogger(RemoteReleaseVlanIdRequest.class);

    private static final String RELEASE_VLAN_ID = "releaseVlanId";
    private static final String VLAN_ID = "vlanId";

    private String provider;
    private int id;

    public RemoteReleaseVlanIdRequest(String provider, int id) {
        this.provider = provider;
        this.id = id;
    }

    @Override
    public Void send() throws Exception {
        IQ iq = RemoteReleaseVlanIdRequest.marshal(this.provider, this.id);
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.provider);

        return null;
    }

    public static IQ marshal(String provider, int id) {
        String vlanId = Integer.toString(id);

        IQ iq = new IQ(IQ.Type.set);
        iq.setTo(provider);
        iq.setID(vlanId);

        Element queryElement = iq.getElement().addElement(IqElement.QUERY.toString(), RELEASE_VLAN_ID);
        Element vlanIdElement = queryElement.addElement(VLAN_ID);
        vlanIdElement.setText(vlanId);

        return iq;
    }
}
