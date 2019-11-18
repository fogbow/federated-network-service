package cloud.fogbow.fns.core.drivers.intercomponent.xmpp.requesters;

import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.core.drivers.dfns.AgentConfiguration;
import cloud.fogbow.fns.core.drivers.intercomponent.xmpp.PacketSenderHolder;
import org.jamppa.component.PacketSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

@PrepareForTest(PacketSenderHolder.class)
public class RemoteConfigureAgentTest extends BaseUnitTest {

    RemoteConfigureAgent remoteConfigureAgent;

    @Before
    public void setup() {
        super.setup();
        this.remoteConfigureAgent = Mockito.spy(new RemoteConfigureAgent(TestUtils.FAKE_PROVIDER,
                TestUtils.FAKE_PUBLIC_KEY, TestUtils.FAKE_SERVICE_NAME));


    }

    // test case: send() should perform the IQ request to make the configuration
    @Test
    public void testSend() throws Exception {
        // setup
        IQ iq = Mockito.mock(IQ.class);
        AgentConfiguration configuration = Mockito.mock(AgentConfiguration.class);

        PacketSender mockedPacketSender = Mockito.mock(PacketSender.class);
        IQ mockedResponse = Mockito.mock(IQ.class);
        Mockito.when(mockedPacketSender.syncSendPacket(Mockito.any())).thenReturn(mockedResponse);

        PowerMockito.mockStatic(PacketSenderHolder.class);
        BDDMockito.given(PacketSenderHolder.getPacketSender()).willReturn(mockedPacketSender);

        Mockito.doReturn(iq).when(remoteConfigureAgent).marshal();
        Mockito.doReturn(configuration).when(remoteConfigureAgent).unmarshal(Mockito.any());

        // exercise
        remoteConfigureAgent.send();

        // verify
        Mockito.verify(remoteConfigureAgent).marshal();
        Mockito.verify(remoteConfigureAgent).unmarshal(Mockito.any());
    }

    // test case: marshal() should build the IQ message to be sent to the remote host
    @Test
    public void testMarshall() {
        // set up
        IQ mockedIq = Mockito.mock(IQ.class);
        Mockito.doReturn(mockedIq).when(remoteConfigureAgent).getIq(Mockito.any());
        Element mockedElement = Mockito.mock(Element.class);
        Mockito.when(mockedIq.getElement()).thenReturn(mockedElement);
        Mockito.when(mockedElement.addElement(Mockito.anyString(), Mockito.anyString())).thenReturn(mockedElement);

        Element mockedQueryElement = Mockito.mock(Element.class);
        Mockito.when(mockedElement.addElement(Mockito.anyString(), Mockito.anyString())).thenReturn(mockedQueryElement);

        Element mockedInstancePublicKeyElement = Mockito.mock(Element.class);
        Element mockedServiceNameElement = Mockito.mock(Element.class);
        Mockito.when(mockedQueryElement.addElement(Mockito.anyString())).thenReturn(mockedInstancePublicKeyElement, mockedServiceNameElement);

        // exercise
        IQ actualIQ = remoteConfigureAgent.marshal();

        // verify
        Assert.assertEquals(mockedIq, actualIQ);
        Mockito.verify(mockedIq).getElement();
        Mockito.verify(mockedQueryElement, Mockito.atLeast(TestUtils.RUN_ONCE)).addElement(Mockito.anyString());
        Mockito.verify(mockedInstancePublicKeyElement).setText(Mockito.anyString());
        Mockito.verify(mockedServiceNameElement).setText(Mockito.anyString());
    }

    // test case: unmarshall(...) should take response content and bring it back to a Java object
    @Test
    public void testUnmarshal() {
        // setup
        IQ responseMock = Mockito.mock(IQ.class);
        Element mockedQueryElement = Mockito.mock(Element.class);
        Element helperElement = Mockito.mock(Element.class);
        Mockito.when(responseMock.getElement()).thenReturn(helperElement);
        Mockito.when(helperElement.element(Mockito.anyString())).thenReturn(mockedQueryElement);

        Element mockedAgentConfiguration = Mockito.mock(Element.class);
        Mockito.when(mockedQueryElement.element(Mockito.anyString())).thenReturn(mockedAgentConfiguration);
        Mockito.when(mockedAgentConfiguration.getText()).thenReturn(TestUtils.EMPTY_JSON_OBJECT_TEXT);

        // exercise
        remoteConfigureAgent.unmarshal(responseMock);

        // verify
        Mockito.verify(mockedAgentConfiguration).getText();
    }
}
