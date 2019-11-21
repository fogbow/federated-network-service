package cloud.fogbow.fns.core.drivers.intercomponent.xmpp.requesters;

import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.PacketSenderHolder;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.ras.core.intercomponent.xmpp.XmppErrorConditionToExceptionTranslator;
import org.jamppa.component.PacketSender;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.xmpp.packet.IQ;

import static org.junit.Assert.*;

@PrepareForTest({PacketSenderHolder.class, XmppErrorConditionToExceptionTranslator.class})
public class RemoteRemoveAgentToComputeTunnelRequestTest extends BaseUnitTest {

    private RemoteRemoveAgentToComputeTunnelRequest remoteRemoveAgentToComputeTunnelRequest;

    @Before
    public void setup() {
        super.setup();
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        remoteRemoveAgentToComputeTunnelRequest = Mockito.spy(new RemoteRemoveAgentToComputeTunnelRequest(TestUtils.FAKE_PROVIDER, order, testUtils.FAKE_IP));
    }

    @Test
    public void testSend() throws Exception {
        PacketSender mockedPacketSender = Mockito.mock(PacketSender.class);
        IQ mockedResponse = Mockito.mock(IQ.class);
        Mockito.when(mockedPacketSender.syncSendPacket(Mockito.any())).thenReturn(mockedResponse);

        PowerMockito.mockStatic(PacketSenderHolder.class);
        BDDMockito.given(PacketSenderHolder.getPacketSender()).willReturn(mockedPacketSender);

        PowerMockito.mockStatic(XmppErrorConditionToExceptionTranslator.class);
        PowerMockito.doNothing().when(XmppErrorConditionToExceptionTranslator.class, "handleError", Mockito.any(), Mockito.any());

        remoteRemoveAgentToComputeTunnelRequest.send();

        Mockito.verify(remoteRemoveAgentToComputeTunnelRequest, Mockito.times(TestUtils.RUN_ONCE)).marshal(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(mockedPacketSender, Mockito.times(TestUtils.RUN_ONCE)).syncSendPacket(Mockito.any());
        PowerMockito.verifyStatic(XmppErrorConditionToExceptionTranslator.class, Mockito.times(TestUtils.RUN_ONCE));
        XmppErrorConditionToExceptionTranslator.handleError(mockedResponse, TestUtils.FAKE_PROVIDER);

    }
}