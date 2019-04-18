package cloud.fogbow.fns.core.intercomponent.xmpp.requesters;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.core.intercomponent.xmpp.RemoteMethod;
import cloud.fogbow.fns.core.intercomponent.xmpp.IqElement;
import cloud.fogbow.ras.core.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import cloud.fogbow.ras.core.intercomponent.xmpp.requesters.RemoteRequest;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.xmpp.packet.IQ;


public class RemoteGetVlanIdRequest implements RemoteRequest<Integer> {
    private static final Logger LOGGER = Logger.getLogger(RemoteGetVlanIdRequest.class);

    private String provider;

    public RemoteGetVlanIdRequest(String provider) {
        this.provider = provider;
    }

    @Override
    public Integer send() throws Exception {
        IQ iq = RemoteGetVlanIdRequest.marshal(this.provider);
        IQ response = (IQ) PacketSenderHolder.getPacketSender().syncSendPacket(iq);

        XmppErrorConditionToExceptionTranslator.handleError(response, this.provider);

        return unmarshalImages(response);
    }

    public static IQ marshal(String provider) {
        IQ iq = new IQ(IQ.Type.get);
        iq.setTo(provider);

        iq.getElement().addElement(IqElement.QUERY.toString(), RemoteMethod.REMOTE_GET_FREE_VLAN_ID.toString());

        return iq;
    }

    private Integer unmarshalImages(IQ response) throws UnexpectedException {
        Element queryElement = response.getElement().element(IqElement.QUERY.toString());
        String listStr = queryElement.element(IqElement.AVAILABLE_VLAN_ID.toString()).getText();

        String instanceClassName = queryElement.element(IqElement.AVAILABLE_VLAN_ID_CLASS_NAME.toString()).getText();

        Integer freeIp;

        try {
            freeIp = (Integer) new Gson().fromJson(listStr, Class.forName(instanceClassName));
        } catch (Exception e) {
            throw new UnexpectedException(e.getMessage());
        }

        return freeIp;
    }
}
