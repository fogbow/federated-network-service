package cloud.fogbow.fns.core;

import cloud.fogbow.as.core.util.AuthenticationUtil;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.common.util.CloudInitUserDataBuilder;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.authorization.DefaultAuthorizationPlugin;
import cloud.fogbow.fns.core.model.*;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import cloud.fogbow.fns.core.serviceconnector.VanillaServiceConnector;
import cloud.fogbow.fns.utils.RedirectToRasUtil;
import cloud.fogbow.ras.api.http.request.Compute;
import cloud.fogbow.ras.api.http.response.ComputeInstance;
import cloud.fogbow.ras.core.models.UserData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthenticationUtil.class, RedirectToRasUtil.class, ServiceConnectorFactory.class})
public class ApplicationFacadeTest extends BaseUnitTest {
    private final String FEDERATED_NETWORK_ID = "fake-network-id";

    private ApplicationFacade applicationFacade;
    private FederatedNetworkOrderController federatedNetworkOrderController;
    private AuthorizationPlugin authPlugin;
    private ComputeRequestsController computeRequestsController;

    @Before
    public void setUp() throws FogbowException {
        this.applicationFacade = Mockito.spy(ApplicationFacade.getInstance());
        federatedNetworkOrderController = Mockito.spy(new FederatedNetworkOrderController());
        authPlugin = Mockito.spy(new DefaultAuthorizationPlugin());
        computeRequestsController = Mockito.spy(new ComputeRequestsController());
        this.applicationFacade.setFederatedNetworkOrderController(federatedNetworkOrderController);
        this.applicationFacade.setAuthorizationPlugin(authPlugin);
        this.applicationFacade.setComputeRequestsController(computeRequestsController);
        PowerMockito.mockStatic(AuthenticationUtil.class);
        Mockito.doReturn(Mockito.mock(RSAPublicKey.class)).when(applicationFacade).getAsPublicKey();
        BDDMockito.given(AuthenticationUtil.authenticate(Mockito.any(), Mockito.any())).willReturn(testUtils.user);
    }

    @Test
    public void testVersion() {
        // Exercise
        String build = this.applicationFacade.getVersionNumber();

        // Test
        Assert.assertEquals(SystemConstants.API_VERSION_NUMBER + "-" + "abcd", build);
    }

    @Test
    public void testCreateFederatedNetwork() throws FogbowException {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doNothing().when(federatedNetworkOrderController).addFederatedNetwork(Mockito.any(), Mockito.any());

        String orderId = applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_TOKEN);

        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).addFederatedNetwork(Mockito.any(), Mockito.any());
        Mockito.verify(authPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
        Assert.assertEquals(order.getId(), orderId);
    }

    @Test
    public void testGetFederatedNetwork() throws FogbowException {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn(order).when(federatedNetworkOrderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        applicationFacade.getFederatedNetwork(FEDERATED_NETWORK_ID, TestUtils.FAKE_TOKEN);

        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetFederatedNetworkStatus() throws FogbowException{
        List<InstanceStatus> instanceStatuses = new ArrayList<>();
        Mockito.doReturn(instanceStatuses).when(federatedNetworkOrderController).getFederatedNetworksStatusByUser(Mockito.any());

        applicationFacade.getFederatedNetworksStatus(TestUtils.FAKE_TOKEN);

        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworksStatusByUser(Mockito.any());
    }

    @Test
    public void testDeleteFederatedNetwork() throws FogbowException{
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn(order).when(federatedNetworkOrderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(federatedNetworkOrderController).deleteFederatedNetwork(Mockito.any());

        applicationFacade.deleteFederatedNetwork(FEDERATED_NETWORK_ID, TestUtils.FAKE_TOKEN);

        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).deleteFederatedNetwork(Mockito.any());
    }

    @Test
    public void testCreateCompute() throws FogbowException {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn("127.0.0.1").when(order).getFreeIp();
        Mockito.doReturn(ConfigurationMode.VANILLA).when(order).getConfigurationMode();
        Mockito.doReturn(order).when(federatedNetworkOrderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).addUserDataToCompute(Mockito.any(), Mockito.any());
        FederatedCompute compute = testUtils.createFederatedCompute(FEDERATED_NETWORK_ID);
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willReturn(new ResponseEntity(testUtils.gson.toJson(compute), HttpStatus.CREATED));
        Mockito.doNothing().when(computeRequestsController).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());

        applicationFacade.createCompute(compute, TestUtils.FAKE_TOKEN);

        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserDataToCompute(Mockito.any(), Mockito.any());
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas("/" + Compute.COMPUTE_ENDPOINT, testUtils.gson.toJson(compute.getCompute()),
            HttpMethod.POST, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testCreateComputeWhenRestClientException() throws FogbowException {
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn("127.0.0.1").when(order).getFreeIp();
        Mockito.doReturn(ConfigurationMode.VANILLA).when(order).getConfigurationMode();
        Mockito.doReturn(order).when(federatedNetworkOrderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).addUserDataToCompute(Mockito.any(), Mockito.any());
        FederatedCompute compute = testUtils.createFederatedCompute(FEDERATED_NETWORK_ID);
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willThrow(new RestClientException(TestUtils.EMPTY_STRING));

        try {
            applicationFacade.createCompute(compute, TestUtils.FAKE_TOKEN);
        } catch (FogbowException ex) {
            Assert.assertEquals(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND, ex.getMessage());
        }

        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserDataToCompute(Mockito.any(), Mockito.any());
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas("/" + Compute.COMPUTE_ENDPOINT, testUtils.gson.toJson(compute.getCompute()),
                HttpMethod.POST, TestUtils.FAKE_TOKEN, String.class);
    }

    @Test
    public void testAddUserDataToComputeWhenUserDataIsNotNull() {
        FederatedCompute federatedCompute = testUtils.createFederatedCompute(FEDERATED_NETWORK_ID);
        ArrayList<UserData> userData = new ArrayList<>();
        UserData userData1 = new UserData("Fake file", CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
        userData.add(userData1);
        federatedCompute.getCompute().setUserData(userData);
        UserData userData2 = new UserData("fake second file", CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);

        applicationFacade.addUserDataToCompute(federatedCompute, userData2);

        Assert.assertEquals(2, federatedCompute.getCompute().getUserData().size());
        Assert.assertTrue(federatedCompute.getCompute().getUserData().contains(userData2));
        Assert.assertTrue(federatedCompute.getCompute().getUserData().contains(userData1));
    }

    @Test
    public void testAddUserDataToComputeWhenUserDataIsNull() {
        FederatedCompute federatedCompute = testUtils.createFederatedCompute(FEDERATED_NETWORK_ID);
        UserData userData1 = new UserData("fake second file", CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);

        applicationFacade.addUserDataToCompute(federatedCompute, userData1);

        Assert.assertEquals(1, federatedCompute.getCompute().getUserData().size());
        Assert.assertTrue(federatedCompute.getCompute().getUserData().contains(userData1));
    }

    @Test
    public void testDeleteCompute() throws FogbowException {
        ComputeInstance computeInstance = new ComputeInstance(TestUtils.FAKE_COMPUTE_ID);
        Mockito.doReturn(computeInstance).when(applicationFacade).getComputeById(Mockito.any(), Mockito.any());
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willReturn(new ResponseEntity(null, HttpStatus.OK));
        Mockito.doNothing().when(computeRequestsController).removeIpToComputeAllocation(Mockito.any());
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn(ConfigurationMode.VANILLA).when(order).getConfigurationMode();
        Mockito.doReturn(order).when(computeRequestsController)
            .getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        Mockito.doNothing().when(applicationFacade).removeAgentToComputeTunnel(Mockito.any(), Mockito.any(), Mockito.any());

        applicationFacade.deleteCompute(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);

        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + TestUtils.FAKE_COMPUTE_ID), "",
                HttpMethod.DELETE, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).removeIpToComputeAllocation(Mockito.any());
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).removeAgentToComputeTunnel(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testDeleteComputeWhenRestClientException() throws Exception {
        ComputeInstance computeInstance = new ComputeInstance(TestUtils.FAKE_COMPUTE_ID);
        Mockito.doReturn(computeInstance).when(applicationFacade).getComputeById(Mockito.any(), Mockito.any());
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willThrow(new RestClientException(TestUtils.EMPTY_STRING));

        try {
            applicationFacade.deleteCompute(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);
        } catch (FogbowException ex) {
            PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
            RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + TestUtils.FAKE_COMPUTE_ID), "",
                    HttpMethod.DELETE, TestUtils.FAKE_TOKEN, String.class);
            Assert.assertEquals(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND, ex.getMessage());
        }
    }

    @Test
    public void testDeleteComputeWhenNullFedNet() throws FogbowException {
        ComputeInstance computeInstance = new ComputeInstance(TestUtils.FAKE_COMPUTE_ID);
        Mockito.doReturn(computeInstance).when(applicationFacade).getComputeById(Mockito.any(), Mockito.any());
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .willReturn(new ResponseEntity(null, HttpStatus.OK));
        Mockito.doNothing().when(computeRequestsController).removeIpToComputeAllocation(Mockito.any());
        Mockito.doReturn(null).when(computeRequestsController)
                .getFederatedNetworkOrderAssociatedToCompute(Mockito.any());

        applicationFacade.deleteCompute(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);

        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + TestUtils.FAKE_COMPUTE_ID), "",
                HttpMethod.DELETE, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).removeIpToComputeAllocation(Mockito.any());
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(0)).removeAgentToComputeTunnel(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetComputeById() throws FogbowException{
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ComputeInstance computeInstance = new ComputeInstance(TestUtils.FAKE_COMPUTE_ID);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willReturn(new ResponseEntity(testUtils.gson.toJson(computeInstance), HttpStatus.OK));
        Mockito.doNothing().when(computeRequestsController).addFederatedIpInGetInstanceIfApplied(Mockito.any(), Mockito.any());

        applicationFacade.getComputeById(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);

        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + TestUtils.FAKE_COMPUTE_ID), "",
                HttpMethod.GET, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).addFederatedIpInGetInstanceIfApplied(Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetComputeByIdWhenRestClientException() throws FogbowException{
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .willThrow(new RestClientException(TestUtils.EMPTY_STRING));

        try {
            applicationFacade.getComputeById(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);
        } catch (FogbowException ex) {
            PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
            RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + TestUtils.FAKE_COMPUTE_ID), "",
                    HttpMethod.GET, TestUtils.FAKE_TOKEN, String.class);
            Assert.assertEquals(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND, ex.getMessage());
        }
    }

    @Test
    public void testAuthorizeOrder() throws FogbowException{
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        order.setSystemUser(testUtils.user);
        Mockito.doReturn(true).when(authPlugin).isAuthorized(Mockito.any(), Mockito.any());

        applicationFacade.authorizeOrder(testUtils.user, Operation.GET, ResourceType.FEDERATED_NETWORK, order);

        Mockito.verify(authPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
    }

    @Test(expected = UnauthorizedRequestException.class)
    public void testAuthorizeOrderWithDifferentRequester() throws FogbowException{
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        order.setSystemUser(testUtils.user);
        Mockito.doReturn(true).when(authPlugin).isAuthorized(Mockito.any(), Mockito.any());

        applicationFacade.authorizeOrder(new SystemUser("fake-id", "fake-name", "fake-provider"),
            Operation.GET, ResourceType.FEDERATED_NETWORK, order);
    }

    @Test
    public void testRemoveAgentToComputeTunnel() throws FogbowException{
        ServiceConnectorFactory serviceConnectorFactory = Mockito.mock(ServiceConnectorFactory.class);
        PowerMockito.mockStatic(ServiceConnectorFactory.class);
        BDDMockito.given(ServiceConnectorFactory.getInstance()).willReturn(serviceConnectorFactory);
        ServiceConnector serviceConnector = Mockito.spy(new VanillaServiceConnector());
        Mockito.when(serviceConnectorFactory.getServiceConnector(Mockito.any(), Mockito.any())).thenReturn(serviceConnector);
        Mockito.doReturn(true).when(serviceConnector).removeAgentToComputeTunnel(Mockito.any(), Mockito.any());
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);

        applicationFacade.removeAgentToComputeTunnel(order, TestUtils.MEMBER, TestUtils.FAKE_IP);

        Mockito.verify(serviceConnector, Mockito.times(TestUtils.RUN_ONCE)).removeAgentToComputeTunnel(Mockito.any(), Mockito.any());
    }

}
