package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
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
import cloud.fogbow.ras.core.models.UserData;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.apache.commons.io.IOUtils;
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
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        PropertiesHolder.class,
        HttpRequestClient.class,
        IOUtils.class
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

        Mockito.when(propertiesMock.getProperty(DriversConfigurationPropertyKeys.Dfns.VLAN_ID_SERVICE_URL_KEY)).thenReturn(TestUtils.FAKE_VLAN_ID_SERVICE_URL);

        Mockito.when(propertiesHolderMock.getProperties(Mockito.anyString())).thenReturn(propertiesMock);

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

        DfnsServiceConnector mockedConnector = getMockedDfnsServiceConnector();
        Mockito.when(mockedConnector.configureAgent(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getMockedSSAgentConfiguration());

        Mockito.doReturn(mockedConnector).when(this.driver).getDfnsServiceConnector(Mockito.anyString());

        // exercise
        this.driver.configureAgent(TestUtils.FAKE_PROVIDER);

        // verify
        Mockito.verify(mockedConnector).configureAgent(Mockito.anyString(), Mockito.anyString());
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
        String providerId = TestUtils.FAKE_PROVIDER_ID;
        String hostIp = TestUtils.FAKE_HOST_IP;

        // exercise
        this.driver.cleanupAgent(providerId, order, hostIp);

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
        String providerId = TestUtils.FAKE_PROVIDER_ID;
        String hostIp = TestUtils.FAKE_HOST_IP;

        // exercise
        this.driver.cleanupAgent(providerId, order, hostIp);

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
    public void testRemoveAgentToComputeTunnel() throws FogbowException {
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

    // test case: the executeAgentCommand should connect to the agent and perform the given command
    @Test
    public void testExecuteAgentCommandSuccessful() throws IOException, FogbowException {
        // setup
        String command = ANY_STRING;
        String exceptionMessage = ANY_STRING;
        String serviceName = ANY_STRING;

        Session.Command commandMock = Mockito.mock(Session.Command.class);
        Mockito.when(commandMock.getExitStatus()).thenReturn(DfnsServiceDriver.SUCCESS_EXIT_CODE);

        Session sessionMock = Mockito.mock(Session.class);
        Mockito.when(sessionMock.exec(Mockito.anyString())).thenReturn(commandMock);

        SSHClient clientMock = Mockito.mock(SSHClient.class);
        Mockito.when(clientMock.startSession()).thenReturn(sessionMock);
        Mockito.doReturn(clientMock).when(driver).getSshClient();

        // exercise
        driver.executeAgentCommand(command, exceptionMessage, serviceName);

        // verify
        Mockito.verify(clientMock).addHostKeyVerifier(Mockito.any(HostKeyVerifier.class));
        Mockito.verify(clientMock).connect(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(clientMock).authPublickey(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(clientMock).startSession();
        Mockito.verify(sessionMock).exec(Mockito.anyString());
        Mockito.verify(commandMock).join();
        Mockito.verify(clientMock).disconnect();
        Mockito.verify(commandMock).getExitStatus();
    }

    // test case: attempt to connect the client when the agent is not available
    // should throw an UnexpectedException
    @Test
    public void testExecuteAgentCommandWithConnectionFail() throws FogbowException, IOException {
        // setup
        String command = ANY_STRING;
        String exceptionMessage = ANY_STRING;
        String serviceName = ANY_STRING;

        SSHClient clientMock = Mockito.mock(SSHClient.class);
        Mockito.doThrow(IOException.class)
                .when(clientMock).connect(Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(clientMock).when(driver).getSshClient();

        // exercise
        try {
            driver.executeAgentCommand(command, exceptionMessage, serviceName);
            Assert.fail();
        } catch (UnexpectedException ex) {
            // verify
            Mockito.verify(clientMock).addHostKeyVerifier(Mockito.any(HostKeyVerifier.class));
            Mockito.verify(clientMock).connect(Mockito.anyString(), Mockito.anyInt());
            Mockito.verify(clientMock).disconnect();
        }

    }

    // test case: failed command execution should throw an UnexpectedException
    @Test
    public void testExecuteAgentCommandWithCommandExecutionFail() throws FogbowException, IOException {
        // setup
        String command = ANY_STRING;
        String exceptionMessage = ANY_STRING;
        String serviceName = ANY_STRING;

        Session.Command commandMock = Mockito.mock(Session.Command.class);
        int unsuccessExitCode = -11;
        Mockito.when(commandMock.getExitStatus()).thenReturn(unsuccessExitCode);

        Session sessionMock = Mockito.mock(Session.class);
        Mockito.when(sessionMock.exec(Mockito.anyString())).thenReturn(commandMock);

        SSHClient clientMock = Mockito.mock(SSHClient.class);
        Mockito.when(clientMock.startSession()).thenReturn(sessionMock);
        Mockito.doReturn(clientMock).when(driver).getSshClient();

        // exercise
        try {
            driver.executeAgentCommand(command, exceptionMessage, serviceName);
            Assert.fail();
        } catch (UnexpectedException ex) {
            // verify
            Assert.assertEquals(exceptionMessage, ex.getMessage());
            Mockito.verify(clientMock).addHostKeyVerifier(Mockito.any(HostKeyVerifier.class));
            Mockito.verify(clientMock).connect(Mockito.anyString(), Mockito.anyInt());
            Mockito.verify(clientMock).disconnect();
        }
    }

    // test case: the getDfnsUserData method should make the appropriate calls to
    // return an UserData object
    @Test
    public void testGetDfnsUserData() throws IOException {
        // setup
        PowerMockito.mockStatic(IOUtils.class);
        BDDMockito.given(IOUtils.toString(Mockito.any(InputStream.class))).willReturn(ANY_STRING);

        SSAgentConfiguration configuration = getMockedSSAgentConfiguration();
        String federatedIp = TestUtils.FAKE_HOST_IP;
        String agentIp = TestUtils.FAKE_HOST_IP;
        int vlanId = ANY_INT;
        String accessKey = ANY_STRING;
        Mockito.doReturn(ANY_STRING).when(driver).replaceScriptTokens(Mockito.anyString(), Mockito.any());

        // Mockito.when(propertiesMock.getProperty(Mockito.eq(DriversConfigurationPropertyKeys
         //       .Dfns.CREATE_TUNNEL_FROM_COMPUTE_TO_AGENT_SCRIPT_PATH_KEY), Mockito.anyString())).thenReturn(ANY_STRING);

        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.doReturn(inputStream).when(driver).getInputStream(Mockito.anyString());

        // exercise
        UserData userData = driver.getDfnsUserData(configuration, federatedIp, agentIp, vlanId, accessKey);

        // verify
        Assert.assertNotNull(userData);
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
