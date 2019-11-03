package cloud.fogbow.fns.core.drivers.intercomponent;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.core.drivers.ServiceDriver;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RemoteFacade.class)
public class RemoteFacadeTest {

    private ServiceDriver driverMock;
    private RemoteFacade remoteFacade;

    @Before
    public void setup() {
        remoteFacade = Mockito.spy(new RemoteFacade());

        PowerMockito.mockStatic(RemoteFacade.class);
        BDDMockito.given(RemoteFacade.getInstance()).willReturn(remoteFacade);

        driverMock = Mockito.mock(ServiceDriver.class);

        Mockito.doReturn(driverMock).when(remoteFacade).getDriver(Mockito.anyString());
    }

    // test case: removeAgentToComputeTunnel should cleanup over order's service
    @Test
    public void testRemoveAgentToComputeTunnel() throws FogbowException {
        // setup
        FederatedNetworkOrder federatedNetworkOrder = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(federatedNetworkOrder.getServiceName()).thenReturn(TestUtils.ANY_STRING);

        // exercise
        remoteFacade.removeAgentToComputeTunnel(TestUtils.FAKE_PROVIDER_ID, federatedNetworkOrder, TestUtils.FAKE_HOST_IP);

        // verify
        Mockito.verify(driverMock).cleanupAgent(TestUtils.FAKE_PROVIDER_ID, Mockito.eq(federatedNetworkOrder), Mockito.eq(TestUtils.FAKE_HOST_IP));
    }



    // test case: configureAgent should return the AgentConfiguration
    @Test
    public void testConfigureAgent() throws FogbowException {
        // setup
        FederatedNetworkOrder federatedNetworkOrder = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(federatedNetworkOrder.getServiceName()).thenReturn(TestUtils.ANY_STRING);
        String publicKey = TestUtils.ANY_STRING;

        // exercise
        remoteFacade.configureAgent(publicKey, TestUtils.FAKE_HOST_IP);

        // verify
        Mockito.verify(driverMock).doConfigureAgent(Mockito.eq(publicKey));
    }
}
