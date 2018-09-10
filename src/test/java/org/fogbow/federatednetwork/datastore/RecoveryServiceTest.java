package org.fogbow.federatednetwork.datastore;

import org.fogbow.federatednetwork.datastore.order_storage.OrderRepository;
import org.fogbow.federatednetwork.datastore.order_storage.RecoveryService;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedOrder;
import org.fogbow.federatednetwork.model.FederatedUser;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbowcloud.ras.core.models.instances.InstanceState;
import org.fogbowcloud.ras.core.models.orders.OrderState;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.*;

@PowerMockIgnore({"javax.management.*", "org.apache.http.conn.ssl.*", "org.apache.http.conn.util.*",
        "javax.net.ssl.*" , "javax.crypto.*"})
@PrepareForTest({DatabaseManager.class})
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest
public class RecoveryServiceTest {

    private static final String FEDERATED_NETWORK_ID = "fake-network-id";
    private static final String USER_ID = "user-id";
    private static final String USER_NAME = "user-name";
    private static final String MEMBER = "member";
    private static final String CIDR = "10.150.0.0/28";

    @Autowired
    private RecoveryService recoveryService;

    @Autowired
    private OrderRepository orderRepository;

    private DatabaseManager databaseManager;
    private FederatedUser user;
    private FederatedNetworkOrder federatedNetworkOrder;

    @Before
    public void setUp() throws SQLException {
        user = new FederatedUser(USER_ID, USER_NAME);
        databaseManager = Mockito.mock(DatabaseManager.class);
        PowerMockito.mockStatic(DatabaseManager.class);
        BDDMockito.given(DatabaseManager.getInstance()).willReturn(databaseManager);
        federatedNetworkOrder = createFederatedNetwork();
    }

    @After
    public void tearDown() {
        for (FederatedOrder order : orderRepository.findAll()) {
            orderRepository.delete(order);
        }
    }

    @Test
    public void testRecoveryFederatedNetwork() throws SubnetAddressesCapacityReachedException, InvalidCidrException,
            SQLException {
        //set up
        FederatedNetworkUtil.getFreeIpForCompute(federatedNetworkOrder);
        FederatedNetworkUtil.getFreeIpForCompute(federatedNetworkOrder);

        Map<String, FederatedOrder> activeOrdersMap = new HashMap<>();
        activeOrdersMap.put(federatedNetworkOrder.getId(), federatedNetworkOrder);
        Mockito.when(databaseManager.retrieveActiveFederatedOrders()).thenReturn(activeOrdersMap);

        //exercise
        recoveryService.put(federatedNetworkOrder);
        List<FederatedOrder> orders = new ArrayList<>(recoveryService.readActiveOrders().values());

        //verify
        Assert.assertEquals(1, orders.size());
        Assert.assertEquals(federatedNetworkOrder, orders.get(0));
    }

    @NotNull
    private FederatedNetworkOrder createFederatedNetwork() {
        Set<String> allowedMembers = new HashSet<>();
        int ipsServed = 1;
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        FederatedNetworkOrder federatedNetworkOrder = new FederatedNetworkOrder(FEDERATED_NETWORK_ID, user, MEMBER, MEMBER, CIDR,
                "label", allowedMembers, ipsServed, freedIps, computesIp);
        federatedNetworkOrder.setOrderStateInTestMode(OrderState.FULFILLED);
        federatedNetworkOrder.setCachedInstanceState(InstanceState.READY);
        return federatedNetworkOrder;
    }
}
