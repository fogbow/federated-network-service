package org.fogbow.federatednetwork.datastore.order_storage;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedUser;
import org.fogbowcloud.ras.core.PropertiesHolder;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

@PrepareForTest(PropertiesHolder.class)
@RunWith(PowerMockRunner.class)
public class OrderTimestampStorageTest {

    private static String databaseFile = "orderStoreTest.sqlite3";
    private static String databaseURL = "jdbc:sqlite:" + databaseFile;

    private static final String USER_ID = "fake-user-id";
    private static final String USER_NAME = "fake-user-name";
    private static final String FEDERATED_NETWORK_ID = "fake-network-id";
    private static final String MEMBER = "member";
    private static final String CIDR = "10.150.0.0/28";

    private OrderTimestampStorage orderStorage;
    private FederatedNetworkOrder federatedNetworkOrder;
    private FederatedUser user;


    @Before
    public void setUp() throws SQLException {
        user = new FederatedUser(USER_ID, USER_NAME);
        federatedNetworkOrder = createFederatedNetwork();

        PropertiesHolder propertiesHolder = Mockito.mock(PropertiesHolder.class);
        Mockito.when(propertiesHolder.getProperty(Mockito.anyString())).thenReturn(databaseURL);

        PowerMockito.mockStatic(PropertiesHolder.class);
        BDDMockito.given(PropertiesHolder.getInstance()).willReturn(propertiesHolder);
        orderStorage = new OrderTimestampStorage();
    }

    @After
    public void tearDown() {
        new File(databaseFile).delete();
    }


    // test case: Adding new order to database and checking its state
    @Test
    public void testAddOrder() throws SQLException {

        // exercise
        orderStorage.addOrder(federatedNetworkOrder);
        Map<String, List<String>> result = orderStorage.selectOrderById(FEDERATED_NETWORK_ID);

        // verify
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(FEDERATED_NETWORK_ID).size());
        Assert.assertEquals("OPEN", result.get(FEDERATED_NETWORK_ID).get(0));
    }


    // test case: Adding the same order to database with two different states.
    @Test
    public void testAddOrderStateChange() throws SQLException {

        // exercise
        orderStorage.addOrder(federatedNetworkOrder);
        federatedNetworkOrder.setOrderStateInTestMode(OrderState.PENDING);
        orderStorage.addOrder(federatedNetworkOrder);

        Map<String, List<String>> result = orderStorage.selectOrderById(FEDERATED_NETWORK_ID);

        // verify
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(2, result.get(FEDERATED_NETWORK_ID).size());
        Assert.assertEquals("OPEN", result.get(FEDERATED_NETWORK_ID).get(0));
        Assert.assertEquals("PENDING", result.get(FEDERATED_NETWORK_ID).get(1));
    }

    // test case: Adding the order with the same state twice and checking the exception
    @Test(expected = SQLException.class)
    public void testAddOrderWithSameState() throws SQLException {
        // exercise
        orderStorage.addOrder(federatedNetworkOrder);
        orderStorage.addOrder(federatedNetworkOrder);
    }

    @NotNull
    private FederatedNetworkOrder createFederatedNetwork() {
        Set<String> allowedMembers = new HashSet<>();
        int ipsServed = 1;
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        FederatedNetworkOrder federatedNetworkOrder = new FederatedNetworkOrder(FEDERATED_NETWORK_ID, user, MEMBER, MEMBER, CIDR,
                "name", allowedMembers, ipsServed, freedIps, computesIp);
        federatedNetworkOrder.setOrderStateInTestMode(OrderState.OPEN);
        federatedNetworkOrder.setCachedInstanceState(InstanceState.READY);
        return federatedNetworkOrder;
    }
}
