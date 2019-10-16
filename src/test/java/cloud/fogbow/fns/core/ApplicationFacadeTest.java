package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.core.authorization.DefaultAuthorizationPlugin;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotSupportedServiceException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.*;

@PrepareForTest({ FederatedNetworkUtil.class })
public class ApplicationFacadeTest extends BaseUnitTest {
    public static final String NON_EXISTENT_SERVICE_NAME = "Non existent service name";

    private ApplicationFacade applicationFacade = Mockito.spy(ApplicationFacade.getInstance());
    private AuthorizationPlugin authorizationPlugin;
    private FederatedNetworkOrderController orderController;

    public void setup() {
        super.setup();
        this.applicationFacade.setServiceListController(Mockito.spy(new ServiceListController()));
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        authorizationPlugin = Mockito.spy(new DefaultAuthorizationPlugin());
        orderController = Mockito.spy(new FederatedNetworkOrderController());
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

}