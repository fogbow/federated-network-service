package cloud.fogbow.fns;

import cloud.fogbow.common.exceptions.InstanceNotFoundException;
import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.exceptions.AgentCommucationException;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.ras.api.http.response.ComputeInstance;
import cloud.fogbow.ras.api.http.response.InstanceState;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AgentCommunicatorUtil.class)
public class FederatedNetworkOrderControllerTest extends MockedFederatedNetworkUnitTests {
    private final String FEDERATED_NETWORK_ID = "fake-network-id";
    private final String FEDERATED_COMPUTE_ID = "fake-compute-id";
    private final String USER_ID = "fake-user-id";
    private final String USER_NAME = "fake-user-name";
    private final String TOKEN_PROVIDER = "token-provider";
    private final String MEMBER = "member";
    private static final String CIDR = "10.10.0.0/24";
    private static final String NET_NAME = "example-name";

    private FederatedNetworkOrder federatedNetworkOrder;
    private SystemUser systemUser;

    @Before
    public void setUp() {
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
        this.systemUser = new SystemUser(USER_ID, USER_NAME, TOKEN_PROVIDER);
        this.federatedNetworkOrder = new FederatedNetworkOrder(FEDERATED_NETWORK_ID, systemUser, MEMBER,
                MEMBER, CIDR, NET_NAME, allowedMembers, freedIps, computesIp, OrderState.OPEN);
    }

    //test case: Tests if the activation order made in federatedNetworkOrderController will call the expected methods
    @Test
    public void testAddingAFederatedNetwork() throws InvalidCidrException, UnexpectedException {
        //set up
        mockSingletons();

        String cidr = "10.10.10.0/24";
        SubnetUtils.SubnetInfo subnetInfo = new SubnetUtils(cidr).getInfo();
        SystemUser user = Mockito.mock(SystemUser.class);

        FederatedNetworkOrder federatedNetworkOrder = Mockito.spy(new FederatedNetworkOrder());
        federatedNetworkOrder.setId(FEDERATED_NETWORK_ID);
        federatedNetworkOrder.setCidr(cidr);

        doNothing().when(federatedNetworkOrder).setOrderState(Mockito.any());

        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        BDDMockito.given(FederatedNetworkUtil.getSubnetInfo(anyString())).willReturn(subnetInfo);
        BDDMockito.given(FederatedNetworkUtil.isSubnetValid(any(SubnetUtils.SubnetInfo.class))).willReturn(true);

        // exercise
        federatedNetworkOrderController.addFederatedNetwork(federatedNetworkOrder, user);

        //verify
        assertEquals(FEDERATED_NETWORK_ID, federatedNetworkOrder.getId());
    }

    //test case: Tests if a delete operation deletes federatedNetwork from activeFederatedNetworks.
    @Test
    public void testDeleteEmptyFederatedNetwork() throws FederatedNetworkNotFoundException, AgentCommucationException,
            SQLException, UnauthorizedRequestException, UnexpectedException, NotEmptyFederatedNetworkException, InstanceNotFoundException {
        //set up
        mockOnlyDatabase();
        FederatedNetworkOrder federatedNetwork = Mockito.spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, this.systemUser,
                "requestingMember", "providingMember"));

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        Mockito.when(AgentCommunicatorUtil.deleteFederatedNetwork(Mockito.anyString())).thenReturn(true);

        federatedNetwork.setOrderState(OrderState.OPEN);
        when(federatedNetwork.getComputeIdsAndIps()).thenReturn(new HashMap<>());

        federatedNetworkOrdersHolder.insertNewOrder(federatedNetwork);

        //exercise
        federatedNetworkOrderController.deleteFederatedNetwork(federatedNetwork);

        FederatedNetworkOrder returnedOrder = federatedNetworkOrderController.getFederatedNetwork(FEDERATED_NETWORK_ID);
        assertEquals(OrderState.CLOSED, returnedOrder.getOrderState());
    }

    //test case: Tests if a delete operation deletes federatedNetwork from activeFederatedNetworks.
    @Test(expected = NotEmptyFederatedNetworkException.class)
    public void testDeleteNotEmptyFederatedNetwork() throws FederatedNetworkNotFoundException, AgentCommucationException,
            SQLException, UnauthorizedRequestException, UnexpectedException, NotEmptyFederatedNetworkException {
        //set up
        mockOnlyDatabase();
        FederatedNetworkOrder federatedNetwork = Mockito.spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, this.systemUser,
                "requestingMember", "providingMember"));

        federatedNetwork.setOrderState(OrderState.OPEN);
        HashMap<String, String> computeIdsAndIps = new HashMap<>();
        computeIdsAndIps.put("someId", "someIp");
        when(federatedNetwork.getComputeIdsAndIps()).thenReturn(computeIdsAndIps);

        federatedNetworkOrdersHolder.insertNewOrder(federatedNetwork);

        //exercise
        federatedNetworkOrderController.deleteFederatedNetwork(federatedNetwork);
    }

    //test case: Tests that can retrieve a federated network stored into activeFederatedNetwork.
    @Test
    public void testGetFederatedNetwork() throws FederatedNetworkNotFoundException {
        //set up
        mockSingletons();
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        when(federatedNetwork.getSystemUser()).thenReturn(systemUser);
        when(federatedNetworkOrdersHolder.getOrder(FEDERATED_NETWORK_ID)).thenReturn(federatedNetwork);
        //exercise
        FederatedNetworkOrder returnedOrder = federatedNetworkOrderController.getFederatedNetwork(FEDERATED_NETWORK_ID);
        //verify
        assertEquals(federatedNetwork, returnedOrder);
    }

    //test case: This test check if a federated network that can't be found, this get operation should throw a FederatedNetworkNotFoundException
    @Test
    public void testGetNotExistentFederatedNetwork() {
        //set up
        mockSingletons();
        try {
            //exercise
            federatedNetworkOrderController.getFederatedNetwork(FEDERATED_NETWORK_ID);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: Tests if get all federated networks will return all federated networks added
    @Test
    public void testGetFederatedNetworks() {
        //set up
        mockSingletons();
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        FederatedNetworkOrder federatedNetwork2 = mock(FederatedNetworkOrder.class);
        String federatedNetworkId2 = FEDERATED_NETWORK_ID + 2;
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getId()).thenReturn(FEDERATED_NETWORK_ID);
        when(federatedNetwork2.getId()).thenReturn(federatedNetworkId2);
        when(federatedNetwork.getSystemUser()).thenReturn(systemUser);
        when(federatedNetwork2.getSystemUser()).thenReturn(systemUser);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetwork2.getOrderState()).thenReturn(OrderState.FULFILLED);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        fakeActiveFederatedNetworks.put(federatedNetworkId2, federatedNetwork2);
        BDDMockito.given(federatedNetworkOrdersHolder.getActiveOrders()).willReturn(fakeActiveFederatedNetworks);
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(federatedNetworkOrderController.getFederatedNetworksStatusByUser(systemUser));
        //verify
        assertEquals(2, federatedNetworks.size());
        assertEquals(FEDERATED_NETWORK_ID, federatedNetworks.get(0).getInstanceId());
        assertEquals(federatedNetworkId2, federatedNetworks.get(1).getInstanceId());
    }

    //test case: Tests if get all federated networks will return only one federated network, since the other one was
    // activated by another user
    @Test
    public void testGetFederatedNetworksWithDifferentUser() {
        //set up
        mockSingletons();
        String nonAuthenticatedUserId = "non-authenticated";
        SystemUser nonAuthenticatedUser = new SystemUser(nonAuthenticatedUserId, USER_NAME, TOKEN_PROVIDER);
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        FederatedNetworkOrder federatedNetwork2 = mock(FederatedNetworkOrder.class);
        String federatedNetworkId2 = FEDERATED_NETWORK_ID + 2;
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getId()).thenReturn(FEDERATED_NETWORK_ID);
        when(federatedNetwork2.getId()).thenReturn(federatedNetworkId2);
        when(federatedNetwork.getSystemUser()).thenReturn(systemUser);
        when(federatedNetwork2.getSystemUser()).thenReturn(nonAuthenticatedUser);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetwork2.getOrderState()).thenReturn(OrderState.FULFILLED);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        fakeActiveFederatedNetworks.put(federatedNetworkId2, federatedNetwork2);
        BDDMockito.given(federatedNetworkOrdersHolder.getActiveOrders()).willReturn(fakeActiveFederatedNetworks);
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(federatedNetworkOrderController.getFederatedNetworksStatusByUser(systemUser));
        //verify
        assertEquals(1, federatedNetworks.size());
        assertEquals(FEDERATED_NETWORK_ID, federatedNetworks.get(0).getInstanceId());
    }

    //test case: Tests if get all in an Empty federated networks list will return correctly
    @Test
    public void testGetEmptyListOfFederatedNetworks() {
        //set up
        mockSingletons();
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(federatedNetworkOrderController.getFederatedNetworksStatusByUser(systemUser));
        //verify
        assertEquals(0, federatedNetworks.size());
    }

    // compute tests
    // test case: Tests if to add a new federated compute, federatedNetworkOrderController makes the correct calls to the collaborators.
    @Test
    public void testAddFederatedCompute() throws UnexpectedException {
        //set up
        mockOnlyDatabase();
        String cidr = "10.10.10.0/24";
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, systemUser, MEMBER,
                MEMBER, cidr, "test", allowedMembers, freedIps, computesIp, OrderState.OPEN));
        federatedNetwork.setOrderState(OrderState.OPEN);
        federatedNetworkOrdersHolder.insertNewOrder(federatedNetwork);

        String federatedIp = "10.10.10.2";
//        ComputeOrder computeOrder = new ComputeOrder();
//        computeOrder.setId(FEDERATED_COMPUTE_ID);

        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        PowerMockito.mockStatic(FederatedComputeUtil.class);

//        UserData fakeUserData = new UserData("", CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
//
//        addUserData(Compute fnsCompute, String federatedComputeIp, String agentPublicIp,
//                String cidr, String preSharedKey)
//
//        BDDMockito.given(FederatedComputeUtil.addUserData(any(Compute.class), anyString(), anyString(), anyString(), anyString(), anyString())).willReturn(incrementedComputeOrder);
//
//        //exercise
//        federatedNetworkOrderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
//        //verify
//        verify(federatedCompute, times(1)).setFederatedIp(federatedIp);
//        PowerMockito.verifyStatic(FederatedNetworkUtil.class, times(1));
//        FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
//        PowerMockito.verifyStatic(FederatedComputeUtil.class, times(1));
//        FederatedComputeUtil.addUserData(any(ComputeOrder.class), anyString(), anyString(), anyString(),
//                anyString());
//        assertNotEquals(computeOrder.getUserData(), federatedNetworkOrdersHolder.getFederatedNetworkOrder(FEDERATED_NETWORK_ID));
    }



    //test case: Tests if get all in an empty federated networks list will return the same computeOrder given as input.
    @Test
    public void testAddNonFederatedCompute() {
        //set up
        mockSingletons();
//        ComputeOrder computeOrder = new ComputeOrder();
//        computeOrder.setId(FEDERATED_COMPUTE_ID);
//        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder("", "", computeOrder));
//        //exercise
//        ComputeOrder computeReturned = federatedNetworkOrderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
//        //verify
//        assertEquals(computeOrder, computeReturned);
    }

    //test case: Tests if updates correctly the compute with the new id
    @Test
    public void testUpdateFederatedComputeId() {
        //set up
        mockSingletons();
        String newId = "fake-compute-new-id";
//        ComputeOrder computeOrder = new ComputeOrder();
//        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_COMPUTE_ID, FEDERATED_NETWORK_ID, computeOrder));
//        federatedCompute.setId(FEDERATED_COMPUTE_ID);
//
//        DatabaseManager database = Mockito.mock(DatabaseManager.class);
//        PowerMockito.mockStatic(DatabaseManager.class);
//        Mockito.doNothing().when(database).put(any(FederatedComputeOrder.class));
//        BDDMockito.given(DatabaseManager.getInstance()).willReturn(database);
//
//        assertEquals(FEDERATED_COMPUTE_ID, federatedCompute.getId());
//        assertNotEquals(newId, federatedCompute.getId());
//        //exercise
//        federatedNetworkOrderController.updateIdOnComputeCreation(federatedCompute, newId);
//        //verify
//        assertNotEquals(FEDERATED_COMPUTE_ID, federatedCompute.getId());
//        assertEquals(newId, federatedCompute.getId());
    }

    //test case: This test should add federated data to federated compute when receives a get method.
    @Test
    public void testGetFederatedCompute() {
        //set up
        mockOnlyDatabase();
        addNetworkIntoActiveOrdersMap();
        addComputeIntoActiveOrdersMap();
        List<String> ipAddresses = new ArrayList<>();
        ipAddresses.add("192.168.0.2");
        ComputeInstance computeInstance = new ComputeInstance(FEDERATED_COMPUTE_ID, InstanceState.READY, "host",
                2, 8, 20, ipAddresses);
        //exercise
//        ComputeInstance federatedComputeInstance = federatedNetworkOrderController.addFederatedIpInGetInstanceIfApplied(computeInstance, user);
//        //verify
//        assertNotEquals(computeInstance, federatedComputeInstance);
//        assertTrue(federatedComputeInstance instanceof FederatedComputeInstance);
//        assertNotNull(((FederatedComputeInstance) federatedComputeInstance).getFederatedIp());
    }

    //test case: This test should throw an UnauthenticatedUserException since, a different user is trying to access it.
    @Test
    public void testGetFederatedComputeWithNonAuthenticatedUser() {
        //set up
        mockOnlyDatabase();
        addNetworkIntoActiveOrdersMap();
        addComputeIntoActiveOrdersMap();
        ComputeInstance computeInstance = new ComputeInstance(FEDERATED_COMPUTE_ID);
        String nonAuthenticatedUserId = "non-authenticated";
        SystemUser nonAuthenticatedUser = new SystemUser(nonAuthenticatedUserId, USER_NAME, TOKEN_PROVIDER);

        //exercise
//        try {
//            federatedNetworkOrderController.addFederatedIpInGetInstanceIfApplied(computeInstance, nonAuthenticatedUser);
//            fail();
//        } catch (UnauthenticatedUserException e) {
//            //verify
//        }
    }

    //test case: A get method to a not federated compute should return exactly the same computeInstance, since it's not a federated compute.
    @Test
    public void testGetNotFederatedCompute() {
        //set up
        mockSingletons();
        List<String> ipAddresses = new ArrayList<>();
        ipAddresses.add("192.168.0.2");
        ComputeInstance computeInstance = new ComputeInstance(FEDERATED_COMPUTE_ID, InstanceState.READY, "host",
                2, 8, 20, ipAddresses);
        //exercise
//        ComputeInstance federatedComputeInstance = federatedNetworkOrderController.addFederatedIpInGetInstanceIfApplied(computeInstance, user);
//        //verify
//        assertEquals(computeInstance, federatedComputeInstance);
//        assertFalse(federatedComputeInstance instanceof FederatedComputeInstance);
    }

    //test case: This test should remove a compute order.
    @Test
    public void testDeleteFederatedCompute() {
        //set up
        mockOnlyDatabase();
        addNetworkIntoActiveOrdersMap();
        addComputeIntoActiveOrdersMap();
//        FederatedComputeOrder federatedCompute = federatedNetworkOrdersHolder.getFederatedCompute(FEDERATED_COMPUTE_ID);
//
//        federatedNetworkOrderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
//        federatedNetworkOrderController.updateIdOnComputeCreation(federatedCompute, FEDERATED_COMPUTE_ID);
//        //pre conditions
//        Collection<FederatedNetworkOrder> orders = federatedNetworkOrdersHolder.getActiveOrders().values();
//        Map<String, FederatedNetworkOrder> federatedNetworks = getFederatedNetworksMap(orders);
//        Map<String, FederatedComputeOrder> federatedComputes = getFederatedComputesMap(orders);
//        FederatedNetworkOrder federatedNetwork = federatedNetworkOrdersHolder.getFederatedNetworkOrder(FEDERATED_NETWORK_ID);
//        federatedNetwork.setSystemUser(user);
//        String computeIp = "10.10.10.1";
//        assertEquals(1, federatedNetworks.size());
//        assertNotNull(federatedComputes.get(FEDERATED_COMPUTE_ID));
//        assertTrue(federatedNetwork.getCacheOfFreeIps().isEmpty());
//        assertEquals(2, federatedNetwork.getIpsServed());
//        assertFalse(federatedNetwork.getComputeIdsAndIps().isEmpty());
//        //exercise
//        federatedNetworkOrderController.deleteCompute(FEDERATED_COMPUTE_ID, user);
//        //verify
//        federatedNetworks = getFederatedNetworksMap(orders);
//        federatedComputes = getFederatedComputesMap(orders);
//        federatedNetwork = federatedNetworks.get(FEDERATED_NETWORK_ID);
//        assertEquals(1, federatedNetworks.size());
//        assertNull(federatedComputes.get(FEDERATED_COMPUTE_ID));
//        assertFalse(federatedNetwork.getCacheOfFreeIps().isEmpty());
//        assertEquals(2, federatedNetwork.getIpsServed());
//        assertTrue(federatedNetwork.getComputeIdsAndIps().isEmpty());
    }

    //test case: A delete in a nonexistent federated compute
    @Test
    public void testRemoveNonFederatedCompute() {
        //set up
        mockOnlyDatabase();
        addNetworkIntoActiveOrdersMap();
        Collection<FederatedNetworkOrder> orders = federatedNetworkOrdersHolder.getActiveOrders().values();
        Map<String, FederatedNetworkOrder> federatedNetworks = getFederatedNetworksMap(orders);
        FederatedNetworkOrder federatedNetworkOrder = federatedNetworks.get(FEDERATED_NETWORK_ID);
        //exercise
//        federatedNetworkOrderController.deleteCompute(FEDERATED_COMPUTE_ID, user);
//        //verify
//        verify(federatedNetworkOrder, never()).removeAssociatedIp(anyString());
    }

    //test case: A delete with a different user must raise an UnauthenticatedUserException.
    @Test
    public void testRemoveFederatedComputeWithDifferentUser() {
        //set up
        mockOnlyDatabase();
        addNetworkIntoActiveOrdersMap();
        addComputeIntoActiveOrdersMap();
        //exercise
//        try {
//            String nonAuthenticatedUserId = "non-authenticated";
//            FederationUserToken nonAuthenticatedUser = new FederationUserToken(nonAuthenticatedUserId, USER_NAME);
//            federatedNetworkOrderController.deleteCompute(FEDERATED_COMPUTE_ID, nonAuthenticatedUser);
//            fail();
//        } catch (UnauthenticatedUserException e) {
//            //verify
//        }
    }

    //test case: Tests rollback in a computeOrder, in case of failing to communicate with resource allocation service.
    @Test
    public void testRollbackInAFailedCompute() {
        //set up
        mockOnlyDatabase();
        addNetworkIntoActiveOrdersMap();
//        ComputeOrder computeOrder = new ComputeOrder();
//        computeOrder.setId(FEDERATED_COMPUTE_ID);
//        computeOrder.setFederationUserToken(user);
//        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));
//        federatedNetworkOrderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
//
//        Collection<FederatedNetworkOrder> orders = federatedNetworkOrdersHolder.getActiveOrders().values();
//        Map<String, FederatedNetworkOrder> federatedNetworks = getFederatedNetworksMap(orders);
//        FederatedNetworkOrder federatedNetworkOrder = federatedNetworks.get(FEDERATED_NETWORK_ID);
//
//        //pre conditions
//        assertFalse(federatedNetworkOrder.getComputeIdsAndIps().isEmpty());
//        assertEquals(2, federatedNetworkOrder.getIpsServed());
//        assertTrue(federatedNetworkOrder.getCacheOfFreeIps().isEmpty());
//        //exercise
//        federatedNetworkOrderController.rollbackInFailedPost(federatedCompute);
//        //verify
//        verify(federatedNetworkOrder, times(1)).removeAssociatedIp(anyString());
//        assertTrue(federatedNetworkOrder.getComputeIdsAndIps().isEmpty());
//        assertEquals(2, federatedNetworkOrder.getIpsServed());
//        assertFalse(federatedNetworkOrder.getCacheOfFreeIps().isEmpty());
//        assertEquals(federatedCompute.getFederatedIp(), federatedNetworkOrder.getCacheOfFreeIps().element());
    }

    @Test(expected = InstanceNotFoundException.class)
    public void testGetNonExistentFederatedNetwork() throws FederatedNetworkNotFoundException {
        // set up
        mockDatabase(new HashMap<>());
        FederatedNetworkOrderController controller = new FederatedNetworkOrderController();

        // verify
        String nonExistentId = "non-existent-id";
        controller.getFederatedNetwork(nonExistentId);
    }

    @Test
    public void testGetFederatedNetworksStatusByUser() {
        // set up
        String member = "fake-member";
        SystemUser user = new SystemUser("user1", USER_NAME, TOKEN_PROVIDER);
        SystemUser unusedUser = new SystemUser("unusedUser", USER_NAME, TOKEN_PROVIDER);

        String id1 = "fake-id-1";
        FederatedNetworkOrder order1 = new FederatedNetworkOrder(id1, user, member, member);
        order1.setOrderStateInTestMode(OrderState.FULFILLED);

        String id2 = "fake-id-2";
        FederatedNetworkOrder order2 = new FederatedNetworkOrder(id2, unusedUser, member, member);
        order2.setOrderStateInTestMode(OrderState.FULFILLED);

        List<FederatedNetworkOrder> expectedFilteredOrders = new ArrayList<>();
        expectedFilteredOrders.add(order1);

        List<InstanceStatus> statusesFromOrders = expectedFilteredOrders
                .stream()
                .map(FederatedNetworkOrderController.orderToInstanceStatus())
                .collect(Collectors.toList());
        Collection<InstanceStatus> expectedResult = new ArrayList<>(statusesFromOrders);

        mockSingletons();
        Map<String, FederatedNetworkOrder> activeOrdersMap = new HashMap<>();
        activeOrdersMap.put(order1.getId(), order1);
        activeOrdersMap.put(order2.getId(), order2);
        Mockito.when(federatedNetworkOrdersHolder.getActiveOrders()).thenReturn(activeOrdersMap);

        // exercise
        Collection<InstanceStatus> federatedNetworksStatusByUser = federatedNetworkOrderController.getFederatedNetworksStatusByUser(user);
        Collection<InstanceStatus> actualResult = new ArrayList<>(federatedNetworksStatusByUser);

        // verify
        Assert.assertEquals(expectedResult, actualResult);
    }

    private void addComputeIntoActiveOrdersMap() {
//        ComputeOrder computeOrder = new ComputeOrder();
//        computeOrder.setId(FEDERATED_COMPUTE_ID);
//        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));
//        federatedCompute.setSystemUser(user);
//        when(federatedCompute.getId()).thenReturn(FEDERATED_COMPUTE_ID);
//        when(federatedCompute.getSystemUser()).thenReturn(user);
//        try {
//            federatedNetworkOrdersHolder.getInstance().insertNewOrder(federatedCompute);
//        } catch (Exception e) {
//            fail();
//        }
    }

    private void addNetworkIntoActiveOrdersMap() {
        String cidr = "10.10.10.0/24";
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, systemUser, MEMBER,
                MEMBER, cidr, "test", allowedMembers, freedIps, computesIp, OrderState.OPEN));
        federatedNetworkOrdersHolder.getInstance().insertNewOrder(federatedNetwork);
    }


    private Map<String, FederatedNetworkOrder> getFederatedNetworksMap(Collection<FederatedNetworkOrder> activeOrdersMap) {
        return activeOrdersMap.stream()
                .collect(Collectors.toMap(order -> order.getId(), order -> order));
    }
}
