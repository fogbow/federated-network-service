package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.common.util.connectivity.HttpRequestClient;
import cloud.fogbow.common.util.connectivity.HttpResponse;
import cloud.fogbow.fns.MockedFederatedNetworkUnitTests;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyKeys;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.ras.core.models.UserData;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        PropertiesHolder.class,
        HttpRequestClient.class
})
public class DfnsServiceDriverTest extends MockedFederatedNetworkUnitTests {

    private PropertiesHolder propertiesHolderMock;
    private Properties propertiesMock;
    private DfnsServiceDriver driver;

    private String ANY_STRING = "any-string";
    private int ANY_INT = 42;

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

    // test case: the getDfnsUserData should delegate part of the work to getDfnsUserData
    @Test
    public void testGetComputeUserData() throws IOException, FogbowException {
        // setup
        AgentConfiguration configuration = Mockito.mock(SSAgentConfiguration.class);
        FederatedCompute compute = Mockito.mock(FederatedCompute.class);
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        String instanceIp = TestUtils.FAKE_INSTANCE_ID;

        UserData userData = Mockito.mock(UserData.class);
        Mockito.doReturn(userData).when(this.driver).getDfnsUserData(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());

        // exercise
        this.driver.getComputeUserData(configuration, compute, order, instanceIp);

        // verify
        Mockito.verify(this.driver).getDfnsUserData(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    // test case: during a local cleanup, it should remove local agent
    @Test
    public void testCleanupAgentLocal() throws FogbowException {
        // setup
        Mockito.doReturn(false).when(this.driver).isRemote(Mockito.anyString());
        Mockito.doNothing().when(this.driver).removeAgentToComputeTunnel(Mockito.any(), Mockito.anyString());
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        String hostIp = TestUtils.FAKE_HOST_IP;

        // exercise
        this.driver.cleanupAgent(order, hostIp);

        // verify
        Mockito.verify(this.driver).removeAgentToComputeTunnel(Mockito.any(), Mockito.anyString());
    }

    // test case: during a local cleanup, it should remove remote agent
    @Test
    public void testCleanupAgentRemote() throws FogbowException {
        // setup
        Mockito.doReturn(true).when(this.driver).isRemote(Mockito.anyString());

        DfnsServiceConnector connector = getMockedDfnsServiceConnector();
        Mockito.doReturn(connector).when(this.driver).getDfnsServiceConnector(Mockito.anyString());

        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        String hostIp = TestUtils.FAKE_HOST_IP;

        // exercise
        this.driver.cleanupAgent(order, hostIp);

        // verify
        Mockito.verify(this.driver).getDfnsServiceConnector(Mockito.anyString());
    }

    // test case: call to doConfigureAgent, should perform appropriate invocations
    @Test
    public void testDoConfigureAgent() throws FogbowException {
        // setup
        Mockito.doNothing().when(this.driver).addKeyToAgentAuthorizedPublicKeys(Mockito.anyString());
        Mockito.when(propertiesMock.getProperty(Mockito.anyString())).thenReturn(ANY_STRING);

        // exercise
        this.driver.doConfigureAgent(TestUtils.FAKE_PUBLIC_KEY);

        // verify
        Mockito.verify(this.driver).addKeyToAgentAuthorizedPublicKeys(Mockito.anyString());
    }

    // test case: the removeAgentToComputeTunnel should delegate his task to executeAgentCommand
    @Test
    public void testExecuteAgentCommand() throws FogbowException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(order.getVlanId()).thenReturn(ANY_INT);

        Mockito.doNothing().when(this.driver).executeAgentCommand(Mockito.any(), Mockito.any(), Mockito.any());

        // exercise
        this.driver.removeAgentToComputeTunnel(order, TestUtils.FAKE_HOST_IP);

        // verify
        Mockito.verify(this.driver).executeAgentCommand(Mockito.any(), Mockito.any(), Mockito.any());
    }

    // test case: the acquireVlanId should return an integer for the vlanId
    @Test
    public void testAcquireVlanIdSuccessful() throws FogbowException {
        // setup
        HttpResponse responseMock = Mockito.mock(HttpResponse.class);
        Mockito.when(responseMock.getContent()).thenReturn(ANY_STRING);
        Mockito.when(responseMock.getHttpCode()).thenReturn(HttpStatus.OK.value());

        PowerMockito.mockStatic(HttpRequestClient.class);
        BDDMockito.given(HttpRequestClient.doGenericRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Map.class)))
                .willReturn(responseMock);

        int currentVlanId = ANY_INT;
        Mockito.doReturn(currentVlanId).when(driver).getVlanIdFromResponse(Mockito.any());

        // exercise
        int actualId = driver.acquireVlanId();

        // verify
        Assert.assertEquals(currentVlanId, actualId);
    }

    // test case: the acquireVlanId should throw an exception for a http error status code
    @Test(expected = NoVlanIdsLeftException.class)
    public void testAcquireVlanIdUnsuccessful() throws FogbowException {
        // setup
        HttpResponse responseMock = Mockito.mock(HttpResponse.class);
        Mockito.when(responseMock.getContent()).thenReturn(ANY_STRING);
        Mockito.when(responseMock.getHttpCode()).thenReturn(HttpStatus.NOT_ACCEPTABLE.value());

        PowerMockito.mockStatic(HttpRequestClient.class);
        BDDMockito.given(HttpRequestClient.doGenericRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Map.class)))
                .willReturn(responseMock);

        // exercise
        driver.acquireVlanId();
        Assert.fail();
    }

    // test case: the releaseVlanId should perform the appropriate request to clean up
    // the resource. No exception must be thrown
    @Test
    public void testReleaseVlanIdSuccessful() throws FogbowException {
        // setup
        int vlanId = ANY_INT;

        HttpResponse responseMock = Mockito.mock(HttpResponse.class);
        Mockito.when(responseMock.getHttpCode()).thenReturn(HttpStatus.OK.value());

        PowerMockito.mockStatic(HttpRequestClient.class);
        BDDMockito.given(HttpRequestClient.doGenericRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Map.class)))
                .willReturn(responseMock);

        // exercise
        this.driver.releaseVlanId(vlanId);

        // verify
        PowerMockito.verifyStatic(HttpRequestClient.class);
        HttpRequestClient.doGenericRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Map.class));
    }

    // test case: the releaseVlanId should throw an exception when the cloud informs that
    // the vlanId doesn't exists
    @Test
    public void testReleaseVlanIdUnsuccessful() throws FogbowException {
        // setup
        int vlanId = ANY_INT;

        HttpResponse responseMock = Mockito.mock(HttpResponse.class);
        Mockito.when(responseMock.getHttpCode()).thenReturn(HttpStatus.NOT_FOUND.value());

        PowerMockito.mockStatic(HttpRequestClient.class);
        BDDMockito.given(HttpRequestClient.doGenericRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Map.class)))
                .willReturn(responseMock);

        // exercise
        try {
            this.driver.releaseVlanId(vlanId);
            Assert.fail();
        } catch (UnexpectedException ex) {
            // verify
            Assert.assertEquals(String.format(Messages.Warn.UNABLE_TO_RELEASE_VLAN_ID, vlanId), ex.getMessage());
        }
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
