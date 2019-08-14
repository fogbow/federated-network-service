package cloud.fogbow.fns.core.datastore.orderstorage;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.ComputeIdToFederatedNetworkIdMapping;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import io.swagger.models.auth.In;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;

@PowerMockIgnore({"javax.management.*"})
@PrepareForTest({ComputeIdToFederatedNetworkIdMapping.class, OrderRepository.class})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest
public class RecoveryServiceTest extends BaseUnitTest {

    private static final String FEDERATED_NETWORK_ID = "fake-network-id";

    private final String FED_NET_ONE = "fedNetOne";
    private final String FED_NET_TWO = "fedNetTwo";
    private final String FED_NET_THREE = "fedNetThree";
    private final Integer THREE_TIMES_CALL = 3;
    private final Integer ONE_TIME_CALL = 1;
    private final Integer ORDERS_AMOUNT = 3;

    @Resource
    private RecoveryService recoveryService;
    @Resource
    private OrderRepository orderRepository;

    @After
    public void tearDown() {
        for (FederatedNetworkOrder order : orderRepository.findAll()) {
            orderRepository.delete(order);
        }
        super.tearDown();
    }

    //test case: test if the save operation works as expected by saving some objects and comparing the list returned by db.
    @Test
    public void testSaveOperation() throws UnexpectedException{
        // setup //exercise
        List<FederatedNetworkOrder> expectedFulfilledOrders = testUtils.populateFedNetDbWithState(OrderState.FULFILLED, ORDERS_AMOUNT, recoveryService);
        List<FederatedNetworkOrder> expectedOpenedOrders = testUtils.populateFedNetDbWithState(OrderState.OPEN, ORDERS_AMOUNT, recoveryService);
        List<FederatedNetworkOrder> expectedClosedOrders = testUtils.populateFedNetDbWithState(OrderState.CLOSED, ORDERS_AMOUNT, recoveryService);
        List<FederatedNetworkOrder> expectedDeactivatedOrders = testUtils.populateFedNetDbWithState(OrderState.DEACTIVATED, ORDERS_AMOUNT, recoveryService);
        List<FederatedNetworkOrder> expectedPartiallyFulfilledOrders = testUtils.populateFedNetDbWithState(OrderState.PARTIALLY_FULFILLED, ORDERS_AMOUNT, recoveryService);
        List<FederatedNetworkOrder> expectedFailedOrders = testUtils.populateFedNetDbWithState(OrderState.FAILED, ORDERS_AMOUNT, recoveryService);
        List<FederatedNetworkOrder> expectedSpawningOrders = testUtils.populateFedNetDbWithState(OrderState.SPAWNING, ORDERS_AMOUNT, recoveryService);

        //verify
        Assert.assertEquals(expectedFulfilledOrders, recoveryService.readActiveOrdersByState(OrderState.FULFILLED));
        Assert.assertEquals(expectedOpenedOrders, recoveryService.readActiveOrdersByState(OrderState.OPEN));
        Assert.assertEquals(expectedClosedOrders, recoveryService.readActiveOrdersByState(OrderState.CLOSED));
        Assert.assertEquals(expectedDeactivatedOrders, recoveryService.readActiveOrdersByState(OrderState.DEACTIVATED));
        Assert.assertEquals(expectedPartiallyFulfilledOrders, recoveryService.readActiveOrdersByState(OrderState.PARTIALLY_FULFILLED));
        Assert.assertEquals(expectedFailedOrders, recoveryService.readActiveOrdersByState(OrderState.FAILED));
        Assert.assertEquals(expectedSpawningOrders, recoveryService.readActiveOrdersByState(OrderState.SPAWNING));
    }

    // test case: testing readActiveOrdersByState operations by verifying if the right calls are made.
    @Test
    public void testServiceRestoreCoreOperations() throws Exception {
        //setup
        ComputeIdToFederatedNetworkIdMapping mapper = Mockito.mock(ComputeIdToFederatedNetworkIdMapping.class);
        PowerMockito.mockStatic(ComputeIdToFederatedNetworkIdMapping.class);
        BDDMockito.given(ComputeIdToFederatedNetworkIdMapping.getInstance()).willReturn(mapper);

        OrderState currentTestSate = OrderState.OPEN;
        FederatedNetworkOrder fedNetOne = Mockito.spy(testUtils.createFederatedNetwork(FED_NET_ONE, currentTestSate));
        FederatedNetworkOrder fedNetTwo = Mockito.spy(testUtils.createFederatedNetwork(FED_NET_TWO, currentTestSate));
        FederatedNetworkOrder fedNetThree = Mockito.spy(testUtils.createFederatedNetwork(FED_NET_THREE, currentTestSate));

        Mockito.doNothing().when(fedNetOne).fillCacheOfFreeIps();
        Mockito.doNothing().when(fedNetTwo).fillCacheOfFreeIps();
        Mockito.doNothing().when(fedNetThree).fillCacheOfFreeIps();

        List<FederatedNetworkOrder> ordersToBeReturned = new ArrayList<>();
        ordersToBeReturned.add(fedNetOne);
        ordersToBeReturned.add(fedNetTwo);
        ordersToBeReturned.add(fedNetThree);

        orderRepository = Mockito.mock(OrderRepository.class);
        PowerMockito.mockStatic(OrderRepository.class);
        Mockito.when(orderRepository.findByOrderState(Mockito.any(OrderState.class))).thenReturn(ordersToBeReturned);
        recoveryService.setOrderRepository(orderRepository);

        //exercise
        recoveryService.readActiveOrdersByState(currentTestSate);

        //verify
        Mockito.verify(mapper, Mockito.times(THREE_TIMES_CALL)).put(Mockito.anyString(), Mockito.anyObject());
        Mockito.verify(fedNetOne, Mockito.times(ONE_TIME_CALL)).fillCacheOfFreeIps();
        Mockito.verify(fedNetTwo, Mockito.times(ONE_TIME_CALL)).fillCacheOfFreeIps();
        Mockito.verify(fedNetThree, Mockito.times(ONE_TIME_CALL)).fillCacheOfFreeIps();
    }
}
