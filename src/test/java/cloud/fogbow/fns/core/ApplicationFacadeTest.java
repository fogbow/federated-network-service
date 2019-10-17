package cloud.fogbow.fns.core;

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
import com.google.gson.Gson;
import kotlin.jvm.internal.unsafe.MonitorKt;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
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

import static org.junit.Assert.*;

@PrepareForTest({ FederatedNetworkUtil.class, RedirectToRasUtil.class })
public class ApplicationFacadeTest extends BaseUnitTest {
    public static final String NON_EXISTENT_SERVICE_NAME = "Non existent service name";

    private ApplicationFacade applicationFacade = Mockito.spy(ApplicationFacade.getInstance());
    private AuthorizationPlugin authorizationPlugin;
    private FederatedNetworkOrderController orderController;
    private ComputeRequestsController computeRequestsController;

    public void setup() {
        super.setup();
        this.applicationFacade.setServiceListController(Mockito.spy(new ServiceListController()));
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        authorizationPlugin = Mockito.spy(new DefaultAuthorizationPlugin());
        orderController = Mockito.spy(new FederatedNetworkOrderController());
        computeRequestsController = Mockito.spy(new ComputeRequestsController());
        applicationFacade.setComputeRequestsController(computeRequestsController);
        applicationFacade.setAuthorizationPlugin(authorizationPlugin);
        applicationFacade.setFederatedNetworkOrderController(orderController);
    }

    @Test(expected = NotSupportedServiceException.class)
    public void testCreateFederatedNetworkWithNotSupportedService() throws FogbowException {
        FederatedNetworkOrder order = new FederatedNetworkOrder();
        order.setServiceName(NON_EXISTENT_SERVICE_NAME);
        applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_USER_TOKEN);

    }

    @Test(expected = InvalidCidrException.class)
    public void testCreateFederatedNetworkWithNotValidSubnet() throws Exception{
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        PowerMockito.doReturn(false).when(FederatedNetworkUtil.class, "isSubnetValid", Mockito.any());

        applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_USER_TOKEN);
    }

    @Test
    public void testCreateFederatedNetwork() throws Exception{
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        PowerMockito.doReturn(true).when(FederatedNetworkUtil.class, "isSubnetValid", Mockito.any());
        Mockito.doReturn(true).when(authorizationPlugin).isAuthorized(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(orderController).activateOrder(Mockito.any());

        String orderId = applicationFacade.createFederatedNetwork(order, TestUtils.FAKE_USER_TOKEN);

        Assert.assertEquals(testUtils.user, order.getSystemUser());
        Assert.assertEquals(order.getId(), orderId);
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).activateOrder(Mockito.any());
        Mockito.verify(authorizationPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetFederatedNetwork() throws Exception{
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        FederatedNetworkOrder returnedOrder = applicationFacade.getFederatedNetwork(order.getId(), TestUtils.FAKE_USER_TOKEN);

        Assert.assertEquals(order, returnedOrder);
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetFederatedNetworksStatus() throws Exception{
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doReturn(true).when(authorizationPlugin).isAuthorized(Mockito.any(), Mockito.any());

        InstanceStatus instanceStatus = new InstanceStatus(TestUtils.FAKE_ID, testUtils.MEMBER, InstanceState.OPEN);
        InstanceStatus secondInstanceStatus = new InstanceStatus("second order id", "fake provider", InstanceState.FAILED);

        Collection<InstanceStatus> ordersStatus = new ArrayList<>();
        ordersStatus.add(instanceStatus);
        ordersStatus.add(secondInstanceStatus);

        Mockito.doReturn(ordersStatus).when(orderController).getFederatedNetworksStatusByUser(Mockito.any());

        Collection<InstanceStatus> returnedStatus = applicationFacade.getFederatedNetworksStatus(TestUtils.FAKE_USER_TOKEN);

        Assert.assertEquals(ordersStatus, returnedStatus);
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(authorizationPlugin, Mockito.times(TestUtils.RUN_ONCE)).isAuthorized(Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetworksStatusByUser(Mockito.any());
    }

    @Test
    public void testDeleteFederatedNetwork() throws Exception{
        FederatedNetworkOrder order = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doNothing().when(applicationFacade).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doNothing().when(orderController).deleteFederatedNetwork(Mockito.any());

        applicationFacade.deleteFederatedNetwork(order.getId(), TestUtils.FAKE_USER_TOKEN);

        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authorizeOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).deleteFederatedNetwork(Mockito.any());

    }

    @Test
    public void testCreateComputeWithoutFednetInSuccessCase() throws Exception{
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);

        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ResponseEntity<String> responseEntity = new ResponseEntity("{\"id\":\"fake-id\"}", null, HttpStatus.CREATED);
        PowerMockito.doReturn(responseEntity).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        String computeId = applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);

        Assert.assertEquals("fake-id", computeId);
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(Mockito.eq("/ras/computes"), Mockito.eq(new Gson().toJson(federatedCompute.getCompute())),
            Mockito.eq(HttpMethod.POST), Mockito.eq(TestUtils.FAKE_USER_TOKEN), Mockito.eq(String.class));
        Mockito.verify(computeRequestsController, Mockito.times(0)).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test(expected = FogbowException.class)
    public void testCreateComputeWithoutFednetInFailureCase() throws Exception{
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);

        PowerMockito.mockStatic(RedirectToRasUtil.class);
        PowerMockito.doThrow(new RestClientException("")).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
    }

    @Test
    public void testCreateComputeWithFednetInSuccessCase() throws Exception{
        FederatedNetworkOrder order = Mockito.spy(testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN));
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);
        federatedCompute.setFederatedNetworkId(order.getId());

        PowerMockito.mockStatic(RedirectToRasUtil.class);
        ResponseEntity<String> responseEntity = new ResponseEntity("{\"id\":\"fake-id\"}", null, HttpStatus.CREATED);
        PowerMockito.doReturn(responseEntity).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(computeRequestsController).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doReturn(testUtils.user).when(applicationFacade).authenticate(Mockito.any());
        Mockito.doNothing().when(applicationFacade).addUserData(Mockito.any(), Mockito.any());
        Mockito.doReturn("192.168.15.10").when(order).getFreeIp();

        String computeId = applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);

        Assert.assertEquals("fake-id", computeId);
        PowerMockito.verifyStatic(RedirectToRasUtil.class, Mockito.times(TestUtils.RUN_ONCE));
        RedirectToRasUtil.createAndSendRequestToRas(Mockito.eq("/ras/computes"), Mockito.eq(new Gson().toJson(federatedCompute.getCompute())),
                Mockito.eq(HttpMethod.POST), Mockito.eq(TestUtils.FAKE_USER_TOKEN), Mockito.eq(String.class));
        Mockito.verify(computeRequestsController, Mockito.times(1)).addIpToComputeAllocation(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserData(Mockito.any(), Mockito.any());
    }

    @Test(expected = FogbowException.class)
    public void testCreateComputeWithFednetInFailureCaseBecauseTheUsersAreDifferents() throws Exception{
        FederatedNetworkOrder order = Mockito.spy(testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN));
        FederatedCompute federatedCompute = new FederatedCompute();
        Compute compute = new Compute();
        compute.setUserData(new ArrayList<>());
        federatedCompute.setCompute(compute);
        federatedCompute.setFederatedNetworkId(order.getId());

        Mockito.doReturn(order).when(orderController).getFederatedNetwork(Mockito.any());
        Mockito.doReturn(new SystemUser("", "", "")).when(applicationFacade).authenticate(Mockito.any());

        applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
    }

    @Test
    public void testCreateComputeWithFednetInFailureCase() throws Exception {
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
        Mockito.doReturn("192.168.15.10").when(order).getFreeIp();

        try {
            applicationFacade.createCompute(federatedCompute, TestUtils.FAKE_USER_TOKEN);
        } catch (FogbowException ex) { }

        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).addUserData(Mockito.any(), Mockito.any());
        Mockito.verify(orderController, Mockito.times(TestUtils.RUN_ONCE)).getFederatedNetwork(Mockito.any());
        Mockito.verify(applicationFacade, Mockito.times(TestUtils.RUN_ONCE)).authenticate(Mockito.any());
    }

    @Test(expected = FogbowException.class)
    public void testDeleteComputeInFailureCase() throws Exception {
        PowerMockito.mockStatic(RedirectToRasUtil.class);
        PowerMockito.doThrow(new RestClientException("")).when(RedirectToRasUtil.class, "createAndSendRequestToRas", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        applicationFacade.deleteCompute(TestUtils.FAKE_ID, TestUtils.FAKE_USER_TOKEN);
    }

    @Test
    public void testDeleteComputeInSuccessCaseWithoutFedNet() {

    }

    @Test
    public void testDeleteComputeInSuccessCaseWithFedNet() {

    }

}