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

    private static final int RUN_TWICE = 2;
    private static final String ANY_iP = "127.0.0.1";
    private static final String FAKE_FILE_1 = "Fake file";
    private static final String FAKE_FILE_2 = "fake second file";
    private static final String FAKE_ID = "fake-id";
    private static final String FAKE_NAME = "fake-name";
    private static final String FAKE_PROVIDER = "fake-provider";
    private static final String FEDERATED_NETWORK_ID = "fake-network-id";
    private static final String PATH_SEPARATOR = "/";

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

    //test case: check if the method makes the expected calls.
    @Test
    public void testCreateFederatedNetwork() throws FogbowException {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doNothing().when(federatedNetworkOrderController).addFederatedNetwork(Mockito.any(), Mockito.any());
        //exercise
        String orderId = applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_TOKEN);
        //verify
        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).addFederatedNetwork(Mockito.any(), Mockito.any());
        Mockito.verify(authPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
        Assert.assertEquals(order.getId(), orderId);
    }

    //test case: check if the method makes the expected calls.
    @Test
    public void testGetFederatedNetwork() throws FogbowException {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn(order).when(federatedNetworkOrderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.getFederatedNetwork(FEDERATED_NETWORK_ID, TestUtils.FAKE_TOKEN);
        //verify
        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    //test case: check if the method makes the expected calls.
    @Test
    public void testGetFederatedNetworkStatus() throws FogbowException {
        //setup
        List<InstanceStatus> instanceStatuses = new ArrayList<>();
        Mockito.doReturn(instanceStatuses).when(federatedNetworkOrderController).getFederatedNetworksStatusByUser(Mockito.any());
        //exercise
        applicationFacade.getFederatedNetworksStatus(TestUtils.FAKE_TOKEN);
        //verify
        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworksStatusByUser(Mockito.any());
    }

    //test case: check if the method makes the expected calls
    @Test
    public void testDeleteFederatedNetwork() throws FogbowException {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn(order).when(federatedNetworkOrderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(federatedNetworkOrderController).deleteFederatedNetwork(Mockito.any());
        //exercise
        applicationFacade.deleteFederatedNetwork(FEDERATED_NETWORK_ID, TestUtils.FAKE_TOKEN);
        //verify
        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(applicationFacade.getAsPublicKey(), TestUtils.FAKE_TOKEN);
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).deleteFederatedNetwork(Mockito.any());
    }

    //test case: check if the method makes the expected calls.
    @Test
    public void testCreateCompute() throws FogbowException {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        Mockito.doReturn(ANY_iP).when(order).getFreeIp();
        Mockito.doReturn(ConfigurationMode.VANILLA).when(order).getConfigurationMode();
        Mockito.doReturn(order).when(federatedNetworkOrderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).addUserDataToCompute(Mockito.any(), Mockito.any());
        FederatedCompute compute = testUtils.createFederatedCompute(FEDERATED_NETWORK_ID);
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willReturn(new ResponseEntity(testUtils.gson.toJson(compute), HttpStatus.CREATED));
        Mockito.doNothing().when(computeRequestsController).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.createCompute(compute, TestUtils.FAKE_TOKEN);
        //verify
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserDataToCompute(Mockito.any(), Mockito.any());
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(PATH_SEPARATOR + Compute.COMPUTE_ENDPOINT, testUtils.gson.toJson(compute.getCompute()),
            HttpMethod.POST, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
    }

    //test case: check if the method throws an exception when an error occurs on RAS
    @Test
    public void testCreateComputeWhenRestClientException() throws FogbowException {
        //setup
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
            //exercise
            applicationFacade.createCompute(compute, TestUtils.FAKE_TOKEN);
            Assert.fail();
        } catch (FogbowException ex) {
            //verify
            Assert.assertEquals(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND, ex.getMessage());
        }
        //verify
        Mockito.verify(federatedNetworkOrderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserDataToCompute(Mockito.any(), Mockito.any());
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(PATH_SEPARATOR + Compute.COMPUTE_ENDPOINT, testUtils.gson.toJson(compute.getCompute()),
                HttpMethod.POST, TestUtils.FAKE_TOKEN, String.class);
    }

    //test case: check if the method modifies the compute as expected.
    @Test
    public void testAddUserDataToComputeWhenUserDataIsNotNull() {
        //setup
        FederatedCompute federatedCompute = testUtils.createFederatedCompute(FEDERATED_NETWORK_ID);
        ArrayList<UserData> userData = new ArrayList<>();
        UserData userData1 = new UserData(FAKE_FILE_1, CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
        userData.add(userData1);
        federatedCompute.getCompute().setUserData(userData);
        UserData userData2 = new UserData(FAKE_FILE_2, CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
        //exercise
        applicationFacade.addUserDataToCompute(federatedCompute, userData2);
        //verify
        Assert.assertEquals(RUN_TWICE, federatedCompute.getCompute().getUserData().size());
        Assert.assertTrue(federatedCompute.getCompute().getUserData().contains(userData2));
        Assert.assertTrue(federatedCompute.getCompute().getUserData().contains(userData1));
    }

    //test case: check if the method modifies the compute as expected when the userData hasn't been set yet
    @Test
    public void testAddUserDataToComputeWhenUserDataIsNull() {
        //setup
        FederatedCompute federatedCompute = testUtils.createFederatedCompute(FEDERATED_NETWORK_ID);
        UserData userData1 = new UserData("fake second file", CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
        //exercise
        applicationFacade.addUserDataToCompute(federatedCompute, userData1);
        //verify
        Assert.assertEquals(1, federatedCompute.getCompute().getUserData().size());
        Assert.assertTrue(federatedCompute.getCompute().getUserData().contains(userData1));
    }

    //test case: check if the method makes the expected calls
    @Test
    public void testDeleteCompute() throws FogbowException {
        //setup
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
        //exercise
        applicationFacade.deleteCompute(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);
        //verify
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas((PATH_SEPARATOR + Compute.COMPUTE_ENDPOINT + PATH_SEPARATOR + TestUtils.FAKE_COMPUTE_ID), "",
                HttpMethod.DELETE, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).removeIpToComputeAllocation(Mockito.any());
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).removeAgentToComputeTunnel(Mockito.any(), Mockito.any(), Mockito.any());
    }


    //test case: check if the method throws an exception when an error occurs on RAS
    @Test
    public void testDeleteComputeWhenRestClientException() throws Exception {
        //setup
        ComputeInstance computeInstance = new ComputeInstance(TestUtils.FAKE_COMPUTE_ID);
        Mockito.doReturn(computeInstance).when(applicationFacade).getComputeById(Mockito.any(), Mockito.any());
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willThrow(new RestClientException(TestUtils.EMPTY_STRING));

        try {
            //exercise
            applicationFacade.deleteCompute(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);
            Assert.fail();
        } catch (FogbowException ex) {
            //verify
            PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
            RedirectToRasUtil.createAndSendRequestToRas((PATH_SEPARATOR + Compute.COMPUTE_ENDPOINT + PATH_SEPARATOR + TestUtils.FAKE_COMPUTE_ID), "",
                    HttpMethod.DELETE, TestUtils.FAKE_TOKEN, String.class);
            Assert.assertEquals(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND, ex.getMessage());
        }
    }


    //test case: check if the method makes the expected calls when the compute is not associate
    //to any fednet
    @Test
    public void testDeleteComputeWhenNullFedNet() throws FogbowException {
        //setup
        ComputeInstance computeInstance = new ComputeInstance(TestUtils.FAKE_COMPUTE_ID);
        Mockito.doReturn(computeInstance).when(applicationFacade).getComputeById(Mockito.any(), Mockito.any());
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .willReturn(new ResponseEntity(null, HttpStatus.OK));
        Mockito.doNothing().when(computeRequestsController).removeIpToComputeAllocation(Mockito.any());
        Mockito.doReturn(null).when(computeRequestsController)
                .getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        //exercise
        applicationFacade.deleteCompute(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);
        //verify
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas((PATH_SEPARATOR + Compute.COMPUTE_ENDPOINT + PATH_SEPARATOR + TestUtils.FAKE_COMPUTE_ID), "",
                HttpMethod.DELETE, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).removeIpToComputeAllocation(Mockito.any());
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(0)).removeAgentToComputeTunnel(Mockito.any(), Mockito.any(), Mockito.any());
    }

    //test case: check if the method makes the expected calls.
    @Test
    public void testGetComputeById() throws FogbowException {
        //setup
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ComputeInstance computeInstance = new ComputeInstance(TestUtils.FAKE_COMPUTE_ID);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .willReturn(new ResponseEntity(testUtils.gson.toJson(computeInstance), HttpStatus.OK));
        Mockito.doNothing().when(computeRequestsController).addFederatedIpInGetInstanceIfApplied(Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.getComputeById(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);
        //verify
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas((PATH_SEPARATOR + Compute.COMPUTE_ENDPOINT + PATH_SEPARATOR + TestUtils.FAKE_COMPUTE_ID), "",
                HttpMethod.GET, TestUtils.FAKE_TOKEN, String.class);
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).addFederatedIpInGetInstanceIfApplied(Mockito.any(), Mockito.any());
    }

    //test case: check if the method throws an exception when an error occurs on RAS
    @Test
    public void testGetComputeByIdWhenRestClientException() throws FogbowException {
        //setup
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        BDDMockito.given(RedirectToRasUtil.createAndSendRequestToRas(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .willThrow(new RestClientException(TestUtils.EMPTY_STRING));

        try {
            //exercise
            applicationFacade.getComputeById(TestUtils.FAKE_COMPUTE_ID, TestUtils.FAKE_TOKEN);
            Assert.fail();
        } catch (FogbowException ex) {
            //verify
            PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
            RedirectToRasUtil.createAndSendRequestToRas((PATH_SEPARATOR + Compute.COMPUTE_ENDPOINT + PATH_SEPARATOR + TestUtils.FAKE_COMPUTE_ID), "",
                    HttpMethod.GET, TestUtils.FAKE_TOKEN, String.class);
            Assert.assertEquals(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND, ex.getMessage());
        }
    }

    //test case: check if the method makes the expected calls.
    @Test
    public void testAuthorizeOrder() throws FogbowException {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        order.setSystemUser(testUtils.user);
        Mockito.doReturn(true).when(authPlugin).isAuthorized(Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.authorizeOrder(testUtils.user, Operation.GET, ResourceType.FEDERATED_NETWORK, order);
        //verify
        Mockito.verify(authPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
    }

    //test case: check if the method throws an exception when the requester is not the order's owner
    @Test(expected = UnauthorizedRequestException.class)//verify
    public void testAuthorizeOrderWithDifferentRequester() throws FogbowException {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        order.setSystemUser(testUtils.user);
        Mockito.doReturn(true).when(authPlugin).isAuthorized(Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.authorizeOrder(new SystemUser(FAKE_ID, FAKE_NAME, FAKE_PROVIDER),
            Operation.GET, ResourceType.FEDERATED_NETWORK, order);
    }

    //test case: check if the method makes the expected call
    @Test
    public void testRemoveAgentToComputeTunnel() throws FogbowException {
        //setup
        ServiceConnectorFactory serviceConnectorFactory = Mockito.mock(ServiceConnectorFactory.class);
        PowerMockito.mockStatic(ServiceConnectorFactory.class);
        BDDMockito.given(ServiceConnectorFactory.getInstance()).willReturn(serviceConnectorFactory);
        ServiceConnector serviceConnector = Mockito.spy(new VanillaServiceConnector());
        Mockito.when(serviceConnectorFactory.getServiceConnector(Mockito.any(), Mockito.any())).thenReturn(serviceConnector);
        Mockito.doReturn(true).when(serviceConnector).removeAgentToComputeTunnel(Mockito.any(), Mockito.any());
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(FEDERATED_NETWORK_ID, OrderState.FULFILLED);
        //exercise
        applicationFacade.removeAgentToComputeTunnel(order, TestUtils.MEMBER, TestUtils.FAKE_IP);
        //verify
        Mockito.verify(serviceConnector, Mockito.times(TestUtils.RUN_ONCE)).removeAgentToComputeTunnel(Mockito.any(), Mockito.any());
    }

}
