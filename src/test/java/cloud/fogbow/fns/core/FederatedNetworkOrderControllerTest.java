package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.InstanceNotFoundException;
import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.exceptions.UnacceptableOperationException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.core.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        FederatedNetworkOrdersHolder.class,
        PropertiesHolder.class,
        OrderStateTransitioner.class
})
public class FederatedNetworkOrderControllerTest extends BaseUnitTest {

    private FederatedNetworkOrderController controller;
    private FederatedNetworkOrdersHolder ordersHolder;
    private PropertiesHolder propertiesHolderMock;
    private Properties propertiesMock;

    @Before
    public void setupTest() throws InternalServerErrorException {
        // mock properties
        this.propertiesMock = Mockito.mock(Properties.class);
        this.propertiesHolderMock = Mockito.mock(PropertiesHolder.class);

        Mockito.when(propertiesHolderMock.getProperties(Mockito.anyString())).thenReturn(propertiesMock);

        PowerMockito.mockStatic(PropertiesHolder.class);
        BDDMockito.given(PropertiesHolder.getInstance()).willReturn(propertiesHolderMock);

        // mock Orders holder
        this.ordersHolder = Mockito.mock(FederatedNetworkOrdersHolder.class);

        PowerMockito.mockStatic(FederatedNetworkOrdersHolder.class);
        BDDMockito.given(FederatedNetworkOrdersHolder.getInstance()).willReturn(ordersHolder);

        // spy controller
        this.controller = Mockito.spy(new FederatedNetworkOrderController());
    }

    // test case: Given an existing order id, getFederatedNetwork should
    // return a FederatedNetworkOrder instance
    @Test
    public void getFederatedNetworkSuccessful() throws InstanceNotFoundException, InternalServerErrorException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(ordersHolder.getOrder(Mockito.anyString())).thenReturn(order);

        // exercise
        FederatedNetworkOrder actualOrder = this.controller.getFederatedNetwork(TestUtils.ANY_STRING);

        // verify
        Assert.assertEquals(order, actualOrder);
    }

    // test case: Given an non-existent order id, getFederatedNetwork should
    // thrown an Exception
    @Test(expected = InstanceNotFoundException.class)
    public void getFederatedNetworkUnsuccessful() throws InstanceNotFoundException, InternalServerErrorException {
        // setup
        Mockito.when(ordersHolder.getOrder(Mockito.anyString())).thenReturn(null);

        // exercise
        FederatedNetworkOrder actualOrder = this.controller.getFederatedNetwork(TestUtils.ANY_STRING);
        Assert.fail();
    }

    // test case: When activating an order, the order should be set to
    // open state.
    @Test
    public void testActivateOrder() throws InternalServerErrorException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);

        // exercise
        this.controller.activateOrder(order);

        // verify
        Mockito.verify(order).setOrderState(Mockito.eq(OrderState.OPEN));
        Mockito.verify(this.ordersHolder).putOrder(Mockito.eq(order));
    }

    // test case: When deleting a fednet with empty ips, it show transition it
    // to closed state
    @Test
    public void testDeleteFederatedNetworkSuccessful() throws FogbowException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(order.getOrderState()).thenReturn(OrderState.OPEN);
        Mockito.when(order.isAssignedIpsEmpty()).thenReturn(true);

        PowerMockito.mockStatic(OrderStateTransitioner.class);

        // exercise
        this.controller.deleteFederatedNetwork(order);

        // verify
        PowerMockito.verifyStatic(OrderStateTransitioner.class);
        OrderStateTransitioner.transition(order, OrderState.CLOSED);
    }

    // test case: When deleting a fednet with ips assigned, it
    // show throw a UnacceptableOperationException
    @Test(expected = UnacceptableOperationException.class)
    public void testDeleteFederatedNetworkFailedByAssignedIps() throws FogbowException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(order.getOrderState()).thenReturn(OrderState.OPEN);
        Mockito.when(order.isAssignedIpsEmpty()).thenReturn(false);

        // exercise
        this.controller.deleteFederatedNetwork(order);

        Assert.fail();
    }

    // test case: When deleting a fednet with ips assigned, it
    // should throw a UnacceptableOperationException
    @Test(expected = UnacceptableOperationException.class)
    public void testDeleteFederatedNetworkFailedByOrderAlreadyFinished() throws FogbowException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(order.getOrderState()).thenReturn(OrderState.FULFILLED);

        // exercise
        this.controller.deleteFederatedNetwork(order);

        Assert.fail();
    }

    // test case: When deactivating a closed order, it should remove it from ordersHolder
    @Test
    public void testDeactivateOrderSuccessful() throws InternalServerErrorException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(order.getOrderState()).thenReturn(OrderState.CLOSED);

        // exercise
        this.controller.deactivateOrder(order);

        // verify
        Mockito.verify(this.ordersHolder).removeOrder(order);
    }

    // test case: When deactivating an non-closed order, it should thw an
    // InternalServerErrorException
    @Test(expected = InternalServerErrorException.class)
    public void testDeactivateOrderUnsuccessful() throws InternalServerErrorException {
        // setup
        FederatedNetworkOrder order = Mockito.mock(FederatedNetworkOrder.class);
        Mockito.when(order.getOrderState()).thenReturn(OrderState.OPEN);

        // exercise
        this.controller.deactivateOrder(order);

        Assert.fail();
    }

    //test case: Tests if get all federated networks will return all federated networks added by this user
    @Test
    public void testGetFederatedNetworksStatusByUser() throws InternalServerErrorException {
        // setup
        Collection<FederatedNetworkOrder> orders = createOrdersFor(testUtils.createSystemUser());
        Map<String, FederatedNetworkOrder> activeOrders = Mockito.mock(Map.class);
        Mockito.when(activeOrders.values()).thenReturn(orders);

        Mockito.when(ordersHolder.getActiveOrders()).thenReturn(activeOrders);
        SystemUser currentUser = testUtils.createSystemUser();


        // exercise
        Collection<InstanceStatus> statuses = this.controller.getInstancesStatus(currentUser);

        // verify
        Assert.assertFalse(statuses.isEmpty());
    }

    private Collection<FederatedNetworkOrder> createOrdersFor(SystemUser systemUser) throws InternalServerErrorException {
        Collection<FederatedNetworkOrder> orders = new ArrayList<>();
        for (OrderState state : OrderState.values()) {
            FederatedNetworkOrder federatedNetworkOrder = Mockito.mock(FederatedNetworkOrder.class);
            Mockito.when(federatedNetworkOrder.getSystemUser()).thenReturn(systemUser);
            Mockito.when(federatedNetworkOrder.getOrderState()).thenReturn(state);
            orders.add(federatedNetworkOrder);
        }
        return orders;
    }
}
