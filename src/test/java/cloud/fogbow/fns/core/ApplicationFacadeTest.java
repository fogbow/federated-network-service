package cloud.fogbow.fns.core;

import cloud.fogbow.as.core.util.AuthenticationUtil;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.common.util.HttpErrorToFogbowExceptionMapper;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.authorization.DefaultAuthorizationPlugin;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.RedirectToRasUtil;
import cloud.fogbow.ras.api.http.ExceptionResponse;
import cloud.fogbow.ras.api.http.request.Compute;
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
@PrepareForTest({AuthenticationUtil.class, RedirectToRasUtil.class, HttpErrorToFogbowExceptionMapper.class})
public class ApplicationFacadeTest extends BaseUnitTest {
    private final String FEDERATED_NETWORK_ID = "fake-network-id";
    private final String USER_ID = "fake-user-id";
    private final String USER_NAME = "fake-user-name";
    private final String TOKEN_PROVIDER = "token-provider";

    private ApplicationFacade applicationFacade;
    private FederatedNetworkOrderController federatedNetworkOrderController;
    private AuthorizationPlugin authPlugin;
    private ComputeRequestsController computeRequestsController;

    @Before
    public void setUp() throws FogbowException{
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
    public void testGetFederatedNetwork() throws FederatedNetworkNotFoundException, FogbowException {
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
    public void testAddUserDataToCompute() {
        
    }

}
