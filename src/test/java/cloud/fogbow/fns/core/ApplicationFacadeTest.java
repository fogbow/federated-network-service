package cloud.fogbow.fns.core;

import cloud.fogbow.as.core.util.AuthenticationUtil;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.authorization.DefaultAuthorizationPlugin;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotSupportedServiceException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.InstanceState;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.fns.utils.RedirectToRasUtil;
import cloud.fogbow.ras.api.parameters.Compute;
import cloud.fogbow.ras.core.models.UserData;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PrepareForTest({ FederatedNetworkUtil.class, RedirectToRasUtil.class, AuthenticationUtil.class })
public class ApplicationFacadeTest extends BaseUnitTest {
    public static final String NON_EXISTENT_SERVICE_NAME = "Non existent service name";
    public static final String FNS_VANILLA_SERVICE = "vanilla";
    public static final String FNS_DFNS_SERVICE = "dfns";
    public static final String COMPUTES_ENDPOINT = "/ras/computes";
    public static final String FAKE_ID = "fake-id";
    public static final String FAKE_BODY = "{\"id\":\"fake-id\"}";
    public static final String SECOND_ORDER_ID = "second order id";
    public static final String FAKE_PROVIDER =  "fake provider";
    public static final String FAKE_IP = "192.168.15.10";


    private ApplicationFacade applicationFacade = Mockito.spy(ApplicationFacade.getInstance());
    private AuthorizationPlugin authorizationPlugin;
    private FederatedNetworkOrderController orderController;
    private ComputeRequestsController computeRequestsController;
    private ServiceListController serviceListController;

    public void setup() {
        super.setup();
        this.applicationFacade.setServiceListController(Mockito.spy(new ServiceListController()));
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        authorizationPlugin = Mockito.spy(new DefaultAuthorizationPlugin());
        orderController = Mockito.spy(new FederatedNetworkOrderController());
        computeRequestsController = Mockito.spy(new ComputeRequestsController());
        serviceListController = Mockito.spy(new ServiceListController());
        applicationFacade.setComputeRequestsController(computeRequestsController);
        applicationFacade.setAuthorizationPlugin(authorizationPlugin);
        applicationFacade.setFederatedNetworkOrderController(orderController);
        applicationFacade.setServiceListController(serviceListController);
    }

    //test case: Check if an exception is thrown when a not supported service is requested
    @Test(expected = NotSupportedServiceException.class) //verify
    public void testCreateFederatedNetworkWithNotSupportedService() throws FogbowException {
        //setup
        FederatedNetworkOrder order = new FederatedNetworkOrder();
        order.setServiceName(NON_EXISTENT_SERVICE_NAME);
        //exercise
        applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_USER_TOKEN);
    }

    //test case: Check create fednet with invalid cidr
    @Test(expected = InvalidCidrException.class)//verify
    public void testCreateFederatedNetworkWithNotValidSubnet() throws Exception{
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        PowerMockito.doReturn(false).when(FederatedNetworkUtil.class, "isSubnetValid", Mockito.any());
        //exercise
        applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_USER_TOKEN);
    }

    //test case: Successful case of createFederatedNetwork
    @Test
    public void testCreateFederatedNetwork() throws Exception {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        PowerMockito.doReturn(true).when(FederatedNetworkUtil.class, "isSubnetValid", Mockito.any());
        Mockito.doReturn(true).when(authorizationPlugin).isAuthorized(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(orderController).activateOrder(Mockito.any());
        //exercise
        String orderId = applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_USER_TOKEN);
        //verify
        Assert.assertEquals(testUtils.user, order.getSystemUser());
        Assert.assertEquals(order.getId(), orderId);
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).activateOrder(Mockito.any());
        Mockito.verify(authorizationPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
    }

    //test case: Check if getFederatedNetwork makes the expected calls.
    @Test
    public void testGetFederatedNetwork() throws Exception {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        //exercise
        FederatedNetworkOrder returnedOrder = applicationFacade.getFederatedNetwork(order.getId(), TestUtils.FAKE_USER_TOKEN);
        //verify
        Assert.assertEquals(order, returnedOrder);
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    //test case: Check if the method makes the expected call in the successful case
    @Test
    public void testGetFederatedNetworksStatus() throws Exception {
        //setup
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doReturn(true).when(authorizationPlugin).isAuthorized(Mockito.any(), Mockito.any());

        InstanceStatus instanceStatus = new InstanceStatus(TestUtils.FAKE_ID, testUtils.MEMBER, InstanceState.OPEN);
        InstanceStatus secondInstanceStatus = new InstanceStatus(SECOND_ORDER_ID, FAKE_PROVIDER, InstanceState.FAILED);

        Collection<InstanceStatus> ordersStatus = new ArrayList<>();
        ordersStatus.add(instanceStatus);
        ordersStatus.add(secondInstanceStatus);

        Mockito.doReturn(ordersStatus).when(orderController).getInstancesStatus(Mockito.any());
        //exercise
        Collection<InstanceStatus> returnedStatus = applicationFacade.getFederatedNetworksStatus(TestUtils.FAKE_USER_TOKEN);
        //verify
        Assert.assertEquals(ordersStatus, returnedStatus);
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(authorizationPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getInstancesStatus(Mockito.any());
    }

    //test case: Check if the method makes the expected calls
    @Test
    public void testDeleteFederatedNetwork() throws Exception {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(orderController).deleteFederatedNetwork(Mockito.any());
        //exercise
        applicationFacade.deleteFederatedNetwork(order.getId(), TestUtils.FAKE_USER_TOKEN);
        //verify
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).deleteFederatedNetwork(Mockito.any());

    }

    //test case: Check if the method makes the expected calls in the successful case
    @Test
    public void testCreateComputeWithoutFednetInSuccessCase() throws Exception {
        //setup
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);

        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ResponseEntity<String> responseEntity = new ResponseEntity(FAKE_BODY, null, HttpStatus.CREATED);
        PowerMockito.doReturn(responseEntity).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        //exercise
        String computeId = applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
        //verify
        Assert.assertEquals(FAKE_ID, computeId);
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(Mockito.eq(COMPUTES_ENDPOINT), Mockito.eq(new Gson().toJson(federatedCompute.getCompute())),
            Mockito.eq(HttpMethod.POST), Mockito.eq(TestUtils.FAKE_USER_TOKEN), Mockito.eq(String.class));
        Mockito.verify(computeRequestsController, Mockito.times(0)).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
    }

    //test case: check if an exception is thrown when ras is offline
    @Test(expected = FogbowException.class) //verify
    public void testCreateComputeWithoutFednetInFailureCase() throws Exception{
        //setup
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);

        PowerMockito.mockStatic(RedirectToRasUtil.class);
        PowerMockito.doThrow(new RestClientException("")).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
    }

    //test case: Check if the method makes the expected calls in success case
    @Test
    public void testCreateComputeWithFednetInSuccessCase() throws Exception{
        //setup
        FederatedNetworkOrder order = Mockito.spy(testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN));
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);
        federatedCompute.setFederatedNetworkId(order.getId());

        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ResponseEntity<String> responseEntity = new ResponseEntity(FAKE_BODY, null, HttpStatus.CREATED);
        PowerMockito.doReturn(responseEntity).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(computeRequestsController).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doNothing().when(applicationFacade).addUserData(Mockito.any(), Mockito.any());
        Mockito.doReturn(FAKE_IP).when(order).getFreeIp();
        //exercise
        String computeId = applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
        //verify
        Assert.assertEquals(FAKE_ID, computeId);
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(Mockito.eq(COMPUTES_ENDPOINT), Mockito.eq(new Gson().toJson(federatedCompute.getCompute())),
                Mockito.eq(HttpMethod.POST), Mockito.eq(TestUtils.FAKE_USER_TOKEN), Mockito.eq(String.class));
        Mockito.verify(computeRequestsController, Mockito.times(1)).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserData(Mockito.any(), Mockito.any());
    }

    //test case: check if an exception is thrown when the requester is different from the fednet's owner
    @Test(expected = FogbowException.class)//verify
    public void testCreateComputeWithFednetInFailureCaseBecauseTheUsersAreDifferents() throws Exception{
        //setup
        FederatedNetworkOrder order = Mockito.spy(testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN));
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);
        federatedCompute.setFederatedNetworkId(order.getId());

        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doReturn(new SystemUser("", "", "")).when(applicationFacade).authenticate(Mockito.any());
        //exercise
        applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
    }

    //test case: check if the expected calls are made when ras is offline and there is a fednetId
    @Test
    public void testCreateComputeWithFednetInFailureCase() throws Exception {
        //setup
        FederatedNetworkOrder order = Mockito.spy(testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN));
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);
        federatedCompute.setFederatedNetworkId(order.getId());

        PowerMockito.mockStatic(RedirectToRasUtil.class);
        PowerMockito.doThrow(new RestClientException("")).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doNothing().when(applicationFacade).addUserData(Mockito.any(), Mockito.any());
        Mockito.doReturn(FAKE_IP).when(order).getFreeIp();

        try {
            //exercise
            applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
            //verify
            Assert.fail();
        } catch (FogbowException ex) { }
        //verify
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserData(Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
    }

    //test case: check if an exception is thrown when ras is offline
    @Test(expected = FogbowException.class)//verify
    public void testDeleteComputeInFailureCase() throws Exception {
        //setup
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        PowerMockito.doThrow(new RestClientException("")).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        //verify
        applicationFacade.deleteCompute(TestUtils.FAKE_ID, TestUtils.FAKE_USER_TOKEN);
    }

    //test case: check if the expected calls are made when there is no fednet
    @Test
    public void testDeleteComputeInSuccessCaseWithoutFedNet() throws Exception {
        //setup
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ResponseEntity<String> responseEntity = new ResponseEntity(FAKE_BODY, null, HttpStatus.CREATED);
        PowerMockito.doReturn(responseEntity).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(null).when(computeRequestsController).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        //exercise
        applicationFacade.deleteCompute(FAKE_ID, TestUtils.FAKE_USER_TOKEN);
        //verify
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(Mockito.eq(COMPUTES_ENDPOINT+"/"+FAKE_ID), Mockito.eq(""),
                Mockito.eq(HttpMethod.DELETE), Mockito.eq(TestUtils.FAKE_USER_TOKEN), Mockito.eq(String.class));
    }

    //test case: check if the method makes the expected calls when there is a fednet
    @Test
    public void testDeleteComputeInSuccessCaseWithFedNet() throws Exception {
        //setup
        FederatedNetworkOrder order = Mockito.spy(testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN));
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ResponseEntity<String> responseEntity = new ResponseEntity(FAKE_BODY, null, HttpStatus.CREATED);
        PowerMockito.doReturn(responseEntity).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(order).when(computeRequestsController).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        Mockito.doNothing().when(order).removeAssociatedIp(Mockito.any());
        //exercise
        applicationFacade.deleteCompute(FAKE_ID, TestUtils.FAKE_USER_TOKEN);
        //verify
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworkOrderAssociatedToCompute(Mockito.any());
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(Mockito.eq(COMPUTES_ENDPOINT+"/"+FAKE_ID), Mockito.eq(""),
                Mockito.eq(HttpMethod.DELETE), Mockito.eq(TestUtils.FAKE_USER_TOKEN), Mockito.eq(String.class));
    }

    //test case: check if an exception is thrown when ras is offline
    @Test(expected = FogbowException.class)//verify
    public void testGetComputeByIdOnFailureCase() throws Exception{
        //setup
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        PowerMockito.doThrow(new RestClientException("")).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.getComputeById(TestUtils.FAKE_ID, TestUtils.FAKE_USER_TOKEN);
    }

    //test case: check if the expected calls are made in the success case
    @Test
    public void testGetComputeByIdOnSuccessCase() throws Exception {
        //setup
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ResponseEntity<String> responseEntity = new ResponseEntity(FAKE_BODY, null, HttpStatus.CREATED);
        PowerMockito.doReturn(responseEntity).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(computeRequestsController).addFederatedIpInGetInstanceIfApplied(Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.getComputeById(FAKE_ID, TestUtils.FAKE_USER_TOKEN);
        //verify
        Mockito.verify(computeRequestsController, Mockito.times(TestUtils.RUN_ONCE)).addFederatedIpInGetInstanceIfApplied(Mockito.any(), Mockito.any());
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(Mockito.eq(COMPUTES_ENDPOINT+"/"+FAKE_ID), Mockito.eq(""),
                Mockito.eq(HttpMethod.GET), Mockito.eq(TestUtils.FAKE_USER_TOKEN), Mockito.eq(String.class));
    }

    //test case: check if the expected calls are made and the result is ok
    @Test
    public void testGetServiceNames() throws Exception {
        //setup
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doReturn(true).when(authorizationPlugin).isAuthorized(Mockito.any(), Mockito.any());
        Mockito.doCallRealMethod().when(serviceListController).getServiceNames();
        //exercise
        List<String> serviceNames = applicationFacade.getServiceNames(TestUtils.FAKE_USER_TOKEN);
        //verify
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(authorizationPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
        Assert.assertTrue(serviceNames.contains(FNS_VANILLA_SERVICE) && serviceNames.contains(FNS_DFNS_SERVICE));
    }

    //test case: check if an exception is thrown when the order's owner is different from the requester
    @Test(expected = FogbowException.class)//verify
    public void testAuthorizeOrderOnFailureCase() throws Exception {
        //setup
        FederatedNetworkOrder order = Mockito.spy(new FederatedNetworkOrder());
        Mockito.doReturn(new SystemUser("", "", "")).when(order).getSystemUser();
        //exercise
        applicationFacade.authorizeOrder(testUtils.user, null, null, order);
    }

    //test case: check if the expected calls are made in the success case
    @Test
    public void testAuthorizeOrderOnSuccessCase() throws Exception {
        //setup
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        Mockito.doReturn(true).when(authorizationPlugin).isAuthorized(Mockito.any(), Mockito.any());
        //exercise
        applicationFacade.authorizeOrder(testUtils.user, null, null, order);
        //verify
        Mockito.verify(authorizationPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
    }

    //test case: check if the method add the userData
    @Test
    public void testAddUserData() {
        //setup
        FederatedCompute fedCompute = new FederatedCompute();
        Compute compute = new Compute();
        fedCompute.setCompute(compute);

        Assert.assertTrue(compute.getUserData() == null);

        UserData userData = new UserData();
        //exercise
        applicationFacade.addUserData(fedCompute, userData);
        //verify
        Assert.assertTrue(compute.getUserData() != null && !compute.getUserData().isEmpty());
    }

    //test case: check if the method makes the expected calls and returns the expected object
    @Test
    public void testAuthenticate() throws Exception {
        //setup
        PowerMockito.mockStatic(AuthenticationUtil.class);
        PowerMockito.doReturn(testUtils.user).when(AuthenticationUtil.class, "authenticate", Mockito.any(), Mockito.any());
        Mockito.doReturn(null).when(applicationFacade).getAsPublicKey();
        //exercise
        SystemUser user = applicationFacade.authenticate(TestUtils.FAKE_USER_TOKEN);
        //verify
        Assert.assertEquals(testUtils.user, user);
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).getAsPublicKey();
        PowerMockito.verifyStatic(AuthenticationUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        AuthenticationUtil.authenticate(null, TestUtils.FAKE_USER_TOKEN);
    }

}