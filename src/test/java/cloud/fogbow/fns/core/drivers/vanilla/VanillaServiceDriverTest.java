package cloud.fogbow.fns.core.drivers.vanilla;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;

import static org.junit.Assert.*;

@PrepareForTest({FederatedNetworkUtil.class})
public class VanillaServiceDriverTest extends BaseUnitTest {

    private VanillaServiceDriver serviceDriver;

    @Before
    public void setup() throws InternalServerErrorException {
        super.setup();
        serviceDriver = Mockito.spy(new VanillaServiceDriver());
    }

    @Test
    public void testProcessOpen() {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork("fake-id", OrderState.OPEN);
        Assert.assertEquals(0, order.getVlanId());

        serviceDriver.processOpen(order);

        assertEquals(-1, order.getVlanId());
    }

    @Test
    public void testProcessSpawningSuccessCase() throws Exception {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork("fake-id", OrderState.SPAWNING);
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        PowerMockito.doReturn(new SubnetUtils(TestUtils.CIDR).getInfo()).when(FederatedNetworkUtil.class, "getSubnetInfo", Mockito.any());
        Mockito.doNothing().when(serviceDriver).createFederatedNetwork(Mockito.any(), Mockito.any());

        serviceDriver.processSpawning(order);

        Mockito.verify(serviceDriver, Mockito.times(TestUtils.RUN_ONCE)).createFederatedNetwork(Mockito.any(), Mockito.any());
        PowerMockito.verifyStatic(FederatedNetworkUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        FederatedNetworkUtil.getSubnetInfo(TestUtils.CIDR);
    }

    @Test(expected = FogbowException.class)
    public void testProcessSpawningFailureCase() throws Exception {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork("fake-id", OrderState.SPAWNING);
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        PowerMockito.doReturn(new SubnetUtils(TestUtils.CIDR).getInfo()).when(FederatedNetworkUtil.class, "getSubnetInfo", Mockito.any());
        Mockito.doThrow(new FogbowException("")).when(serviceDriver).createFederatedNetwork(Mockito.any(), Mockito.any());

        serviceDriver.processSpawning(order);
    }

    @Test
    public void testProcessClosed() throws Exception {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork("fake-id", OrderState.CLOSED);

        Mockito.doNothing().when(serviceDriver).deleteFederatedNetwork(Mockito.any());

        serviceDriver.processClosed(order);

        Mockito.verify(serviceDriver, Mockito.times(TestUtils.RUN_ONCE)).deleteFederatedNetwork(Mockito.any());
    }

    @Test
    public void testGetComputeUserData() throws Exception {
        Mockito.doReturn(new UserData()).when(serviceDriver).getVanillaUserData(Mockito.any(), Mockito.any());

        serviceDriver.getComputeUserData(null, new FederatedCompute(), new FederatedNetworkOrder(), testUtils.FAKE_IP);

        Mockito.verify(serviceDriver, Mockito.times(TestUtils.RUN_ONCE)).getVanillaUserData(Mockito.any(), Mockito.any());
    }

    @Test(expected = FogbowException.class)
    public void testGetComputeUserDataFailureCase() throws Exception {
        Mockito.doThrow(new IOException()).when(serviceDriver).getVanillaUserData(Mockito.any(), Mockito.any());

        serviceDriver.getComputeUserData(null, new FederatedCompute(), new FederatedNetworkOrder(), testUtils.FAKE_IP);
    }

    @Test
    public void testGetAgentIp() {
        Assert.assertEquals(serviceDriver.getAgentIp(), "100.0.0.0");
    }

}