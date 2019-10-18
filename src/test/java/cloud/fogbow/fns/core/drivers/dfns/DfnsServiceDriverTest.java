package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.MockedFederatedNetworkUnitTests;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyKeys;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        PropertiesHolder.class
})
public class DfnsServiceDriverTest extends MockedFederatedNetworkUnitTests {

    private PropertiesHolder propertiesHolderMock;
    private Properties propertiesMock;
    private DfnsServiceDriver driver;

    @Before
    public void setupTest() {
        // mock properties
        this.propertiesMock = Mockito.mock(Properties.class);
        this.propertiesHolderMock = Mockito.mock(PropertiesHolder.class);

        Mockito.when(propertiesHolderMock.getProperties(Mockito.anyString())).thenReturn(propertiesMock);

        Mockito.when(propertiesMock.getProperty(DriversConfigurationPropertyKeys.Dfns.VLAN_ID_SERVICE_URL_KEY)).thenReturn(TestUtils.FAKE_VLAN_ID_SERVICE_URL);
        Mockito.when(propertiesMock.getProperty(DriversConfigurationPropertyKeys.Dfns.LOCAL_MEMBER_NAME_KEY)).thenReturn(TestUtils.FAKE_LOCAL_MEMBER_NAME);

        PowerMockito.mockStatic(PropertiesHolder.class);
        BDDMockito.given(PropertiesHolder.getInstance()).willReturn(propertiesHolderMock);

        this.driver = Mockito.spy(new DfnsServiceDriver());
    }

    // test case: processOpen should set order's vlan id
    @Test
    public void testProcessOpen() throws FogbowException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.doReturn(TestUtils.FAKE_VLAN_ID).when(driver).acquireVlanId();

        // exercise
        this.driver.processOpen(order);

        // verify
        Mockito.verify(order).setVlanId(Mockito.anyInt());
    }

    // test case: processSpawning should move all providers configurations states to success
    @Test
    public void testProcessSpawning() {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);

        Map<String, MemberConfigurationState> providers = new HashMap();
        providers.put(TestUtils.FAKE_PROVIDER, MemberConfigurationState.UNDEFINED);

        Mockito.when(order.getProviders()).thenReturn(providers);

        // exercise
        this.driver.processSpawning(order);

        // verify
        Assert.assertEquals(MemberConfigurationState.SUCCESS, providers.get(TestUtils.FAKE_PROVIDER));
    }

    // test case: processSpawning should move all providers configurations states to success
    @Test
    public void testProcessClosed() throws FogbowException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);

        Map<String, MemberConfigurationState> providers = new HashMap();
        providers.put(TestUtils.FAKE_PROVIDER, MemberConfigurationState.UNDEFINED);

        Mockito.when(order.getProviders()).thenReturn(providers);

        Mockito.doNothing().when(driver).releaseVlanId(Mockito.anyInt());

        // exercise
        this.driver.processClosed(order);

        // verify
        Assert.assertEquals(MemberConfigurationState.REMOVED, providers.get(TestUtils.FAKE_PROVIDER));
        Mockito.verify(driver).releaseVlanId(Mockito.anyInt());
        Mockito.verify(order).setVlanId(Mockito.anyInt());
    }

    // test case: When configuring a local agent, the driver should perform the local configuration
    @Test
    public void testConfigureAgentLocal() throws FogbowException {
        // setup
        Mockito.doReturn(false).when(this.driver).isRemote(Mockito.anyString());

        SSAgentConfiguration mockedAgentConfiguration = getMockedSSAgentConfiguration();
        Mockito.doReturn(mockedAgentConfiguration).when(this.driver).doConfigureAgent(Mockito.anyString());

        // exercise
        AgentConfiguration configuration = this.driver.configureAgent(TestUtils.FAKE_PROVIDER);

        // verify
        Mockito.verify(this.driver).doConfigureAgent(Mockito.anyString());
    }

    // test case: When configuring a remote agent, the driver should perform the remote configuration
    @Test
    public void testConfigureAgentRemote() throws FogbowException {
        // setup
        Mockito.doReturn(true).when(this.driver).isRemote(Mockito.anyString());

        DfnsServiceConnector mockedAgentConfiguration = getMockedDfnsServiceConnector();
        Mockito.when(mockedAgentConfiguration.configureAgent(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getMockedSSAgentConfiguration());

        Mockito.doReturn(mockedAgentConfiguration).when(this.driver).getDfnsServiceConnector(Mockito.anyString());

        // exercise
        this.driver.configureAgent(TestUtils.FAKE_PROVIDER);

        // verify
        Mockito.verify(mockedAgentConfiguration).configureAgent(Mockito.anyString(), Mockito.anyString());
    }

    private SSAgentConfiguration getMockedSSAgentConfiguration() {
        SSAgentConfiguration mock = Mockito.mock(SSAgentConfiguration.class);
        return mock;
    }

    private DfnsServiceConnector getMockedDfnsServiceConnector() {
        DfnsServiceConnector mock = Mockito.mock(DfnsServiceConnector.class);
        return mock;
    }
}
