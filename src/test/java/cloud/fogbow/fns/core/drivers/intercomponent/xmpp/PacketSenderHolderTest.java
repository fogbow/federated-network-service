package cloud.fogbow.fns.core.drivers.intercomponent.xmpp;

import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xmpp.component.ComponentException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacketSenderHolder.class})
public class PacketSenderHolderTest extends BaseUnitTest {

    private static final String METHOD_INIT = "init";
    private static final String XMPP_JID = "jid";
    private static final String XMPP_PASSWORD = "password";
    private static final String XMPP_SERVER_IP = "server-ip";
    private static final String XMPP_C2C_PORT = "2474";
    private static final String XMPP_TIMEOUT = "10000";

    private PacketSenderHolder packetSenderHolder;

    @Before
    public void setup() {
        super.setup();
        packetSenderHolder = Mockito.spy(new PacketSenderHolder());
    }

    // test case: The init method should create the component manager and establish a connection
    @Test
    public void testInitSuccessFul() throws Exception {
        // setup
        mockXmppProperties();
        Mockito.when(propertiesHolderMock.getProperty(ConfigurationPropertyKeys.XMPP_SERVER_IP_KEY)).thenReturn(XMPP_SERVER_IP);

        XmppComponentManager xmppComponentManager = Mockito.mock(XmppComponentManager.class);
        PowerMockito.mockStatic(PacketSenderHolder.class);
        PowerMockito.doCallRealMethod().when(PacketSenderHolder.class, METHOD_INIT);
        BDDMockito.given(PacketSenderHolder.getXmppComponentManager(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).willReturn(xmppComponentManager);

        // exercise
        PacketSenderHolder.init();

        // verify
        Mockito.verify(xmppComponentManager).connect();
    }

    // test case: When the componentManger can't establish a connection, an IllegalStateException
    // should be thrown.
    @Test
    public void testInitFailWIthUnavailableConnection() throws Exception {
        mockXmppProperties();
        Mockito.when(propertiesHolderMock.getProperty(ConfigurationPropertyKeys.XMPP_SERVER_IP_KEY)).thenReturn(XMPP_SERVER_IP);

        XmppComponentManager xmppComponentManager = Mockito.mock(XmppComponentManager.class);
        Mockito.doThrow(ComponentException.class).when(xmppComponentManager).connect();
        PowerMockito.mockStatic(PacketSenderHolder.class);
        PowerMockito.doCallRealMethod().when(PacketSenderHolder.class, METHOD_INIT);
        BDDMockito.given(PacketSenderHolder.getXmppComponentManager(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).willReturn(xmppComponentManager);

        try {
            // exercise
            PacketSenderHolder.init();
            Assert.fail();
        } catch (IllegalStateException ex) {
            // verify
            Mockito.verify(xmppComponentManager).connect();
        }
    }

    private void mockXmppProperties() {
        Mockito.when(propertiesHolderMock.getProperty(ConfigurationPropertyKeys.XMPP_PASSWORD_KEY)).thenReturn(XMPP_PASSWORD);
        Mockito.when(propertiesHolderMock.getPropertyOrDefault(Mockito.eq(ConfigurationPropertyKeys.XMPP_C2C_PORT_KEY), Mockito.anyString())).thenReturn(XMPP_C2C_PORT);
        Mockito.when(propertiesHolderMock.getPropertyOrDefault(Mockito.eq(ConfigurationPropertyKeys.XMPP_TIMEOUT_KEY), Mockito.anyString())).thenReturn(XMPP_TIMEOUT);
    }
}
