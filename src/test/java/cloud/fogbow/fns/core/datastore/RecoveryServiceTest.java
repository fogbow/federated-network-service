package cloud.fogbow.fns.core.datastore;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.datastore.orderstorage.OrderRepository;
import cloud.fogbow.fns.core.datastore.orderstorage.RecoveryService;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import org.jetbrains.annotations.NotNull;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;

@PowerMockIgnore({"javax.management.*"})
@PrepareForTest({DatabaseManager.class})
@PowerMockRunnerDelegate(SpringRunner.class)
@RunWith(PowerMockRunner.class)
@SpringBootTest
public class RecoveryServiceTest {

    private static final String FEDERATED_NETWORK_ID = "fake-network-id";
    private static final String USER_ID = "user-id";
    private static final String USER_NAME = "user-name";
    private static final String MEMBER = "member";
    private static final String CIDR = "10.150.0.0/28";

    @Autowired
    private RecoveryService recoveryService;

    @Resource
    private OrderRepository orderRepository;

    @MockBean
    private AuditService auditService;

    private DatabaseManager databaseManager;
    private SystemUser user;
    private FederatedNetworkOrder federatedNetworkOrder;

    @Before
    public void setUp() {
        user = new SystemUser(USER_ID, USER_NAME, MEMBER);
        databaseManager = Mockito.mock(DatabaseManager.class);
        Mockito.when(databaseManager.readActiveOrders(OrderState.OPEN)).thenReturn(new SynchronizedDoublyLinkedList<>());
        Mockito.when(databaseManager.readActiveOrders(OrderState.FULFILLED)).thenReturn(new SynchronizedDoublyLinkedList<>());
        Mockito.when(databaseManager.readActiveOrders(OrderState.FAILED)).thenReturn(new SynchronizedDoublyLinkedList<>());
        Mockito.when(databaseManager.readActiveOrders(OrderState.CLOSED)).thenReturn(new SynchronizedDoublyLinkedList<>());
        PowerMockito.mockStatic(DatabaseManager.class);
        BDDMockito.given(DatabaseManager.getInstance()).willReturn(databaseManager);
        federatedNetworkOrder = createFederatedNetwork();
    }

    @After
    public void tearDown() {
        for (FederatedNetworkOrder order : orderRepository.findAll()) {
            orderRepository.delete(order);
        }
    }

//    @Test
//    public void testRecoveryFederatedNetwork() throws UnexpectedException {
//        //set up
//        Map<String, FederatedNetworkOrder> activeOrdersMap = new HashMap<>();
//        activeOrdersMap.put(federatedNetworkOrder.getId(), federatedNetworkOrder);
//        Mockito.when(databaseManager.retrieveActiveFederatedOrders()).thenReturn(activeOrdersMap);
//
//        //exercise
//        recoveryService.put(federatedNetworkOrder);
//        List<FederatedNetworkOrder> orders = new ArrayList<>(recoveryService.readActiveOrders().values());
//
//        //verify
//        Assert.assertEquals(1, orders.size());
//        Assert.assertEquals(federatedNetworkOrder, orders.get(0));
//    }
//
    @NotNull
    private FederatedNetworkOrder createFederatedNetwork() {
        HashMap<String, MemberConfigurationState> allowedMembers = new HashMap<>();
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        FederatedNetworkOrder federatedNetworkOrder = new FederatedNetworkOrder(FEDERATED_NETWORK_ID, user, MEMBER, MEMBER, CIDR,
                "name", allowedMembers, freedIps, computesIp, OrderState.OPEN);
        federatedNetworkOrder.setOrderStateInTestMode(OrderState.FULFILLED);
        return federatedNetworkOrder;
    }

    @Test
    public void bla() throws UnexpectedException{
        System.out.println(federatedNetworkOrder);
        recoveryService.put(federatedNetworkOrder);
        System.out.println(recoveryService.readActiveOrdersByState(OrderState.FULFILLED).size());
    }
}
