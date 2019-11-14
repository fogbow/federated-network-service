package cloud.fogbow.fns.core.drivers.intercomponent.xmpp;

import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
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
        } catch (RuntimeException ex) {
            // verify
            Mockito.verify(xmppComponentManager).connect();
        }
    }
}
