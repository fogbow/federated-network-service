package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.model.*;
import org.fogbow.federatednetwork.utils.AgentCommunicatorUtil;
import org.fogbow.federatednetwork.utils.FederatedComputeUtil;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.fogbowcloud.ras.core.models.instances.ComputeInstance;
import org.fogbowcloud.ras.core.models.instances.InstanceState;
import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AgentCommunicatorUtil.class, FederatedNetworkUtil.class, FederatedComputeUtil.class,
        DatabaseManager.class, FederatedNetworkOrdersHolder.class})
public class FederatedNetworkOrderControllerTest extends MockedFederatedNetworkUnitTests{

    private final String FEDERATED_NETWORK_ID = "fake-network-id";
    private final String FEDERATED_COMPUTE_ID = "fake-compute-id";
    private final String USER_ID = "fake-user-id";
    private final String USER_NAME = "fake-user-name";
    private final String TOKEN_PROVIDER = "token-provider";
    private final String TOKEN_USER_VALUE = "token-value";
    private final String MEMBER = "member";

    private Properties properties;
    private FederationUserToken user;


    @Before
    public void setUp() {
        properties = super.setProperties();
        user = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, USER_ID, USER_NAME);
    }

    //test case: Tests if the activation order made in federatedNetworkOrderController will call the expected methods
    @Test
    public void testActivatingFederatedNetwork() throws InvalidCidrException {
        //set up
        mockSingletons();
        String fakeCidr = "10.10.10.0/24";
        SubnetUtils.SubnetInfo fakeSubnetInfo = new SubnetUtils(fakeCidr).getInfo();
        FederationUserToken user = mock(FederationUserToken.class);
        FederatedNetworkOrder federatedNetworkOrder = spy(new FederatedNetworkOrder());
        federatedNetworkOrder.setId(FEDERATED_NETWORK_ID);
        federatedNetworkOrder.setCidr(fakeCidr);
        doNothing().when(federatedNetworkOrder).setOrderState(OrderState.FULFILLED);
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        BDDMockito.given(FederatedNetworkUtil.getSubnetInfo(anyString())).willReturn(fakeSubnetInfo);
        BDDMockito.given(FederatedNetworkUtil.isSubnetValid(any(SubnetUtils.SubnetInfo.class))).willReturn(true);
        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.createFederatedNetwork(anyString(), anyString()))
                .willReturn(true);
        // exercise
        federatedNetworkOrderController.activateFederatedNetwork(federatedNetworkOrder, user);
        //verify
        verify(federatedNetworkOrder, times(1)).setOrderState(OrderState.FULFILLED);
        assertEquals(FEDERATED_NETWORK_ID, federatedNetworkOrder.getId());
    }

    //test case: Tests if any error in communication with agent, will lead the order to fail
    @Test
    public void testAgentCommunicationError() throws InvalidCidrException {
        //set up
        mockSingletons();
        String fakeCidr = "10.10.10.0/24";
        SubnetUtils.SubnetInfo fakeSubnetInfo = new SubnetUtils(fakeCidr).getInfo();
        FederationUserToken user = mock(FederationUserToken.class);
        FederatedNetworkOrder federatedNetworkOrder = spy(new FederatedNetworkOrder());
        federatedNetworkOrder.setId(FEDERATED_NETWORK_ID);
        federatedNetworkOrder.setCidr(fakeCidr);
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        BDDMockito.given(FederatedNetworkUtil.getSubnetInfo(anyString())).willReturn(fakeSubnetInfo);
        BDDMockito.given(FederatedNetworkUtil.isSubnetValid(any(SubnetUtils.SubnetInfo.class))).willReturn(true);
        doNothing().when(federatedNetworkOrder).setOrderState(OrderState.FULFILLED);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.createFederatedNetwork(anyString(), anyString())).willReturn(false);
        // exercise
        federatedNetworkOrderController.activateFederatedNetwork(federatedNetworkOrder, user);
        assertEquals(federatedNetworkOrder.getOrderState(), OrderState.FAILED);
    }

    //test case: Tests that can retrieve a federated network stored into activeFederatedNetwork.
    @Test
    public void testGetFederatedNetwork() throws SQLException, UnauthorizedRequestException {
        //set up
        mockSingletons();
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        when(federatedNetwork.getUser()).thenReturn(user);
        when(federatedNetworkOrdersHolder.getOrder(FEDERATED_NETWORK_ID)).thenReturn(federatedNetwork);
        //exercise
        try {
            FederatedNetworkOrder returnedOrder = federatedNetworkOrderController.getFederatedNetwork(FEDERATED_NETWORK_ID, user);
            //verify
            assertEquals(federatedNetwork, returnedOrder);
        } catch (FederatedNetworkNotFoundException e) {
            fail();
        }
    }

    //test case: Trying to retrieve a federated network  from another user must throw UnauthenticatedUserException.
    @Test
    public void testGetFederatedNetworkWithDifferentUser() throws FederatedNetworkNotFoundException {
        //set up
        mockSingletons();
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        when(federatedNetwork.getUser()).thenReturn(user);
        when(federatedNetworkOrdersHolder.getOrder(FEDERATED_NETWORK_ID)).thenReturn(federatedNetwork);

        String nonAuthenticatedUserId = "non-autheticated";
        FederationUserToken nonAuthenticatedUser = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, nonAuthenticatedUserId, USER_NAME);
        //exercise
        try {
            federatedNetworkOrderController.getFederatedNetwork(FEDERATED_NETWORK_ID, nonAuthenticatedUser);
            fail();
        } catch (UnauthorizedRequestException e) {
            //verify
        }
    }

    //test case: This test check if a federated network that can't be found, this get operation should throw a FederatedNetworkNotFoundException
    @Test
    public void testGetNotExistentFederatedNetwork() throws UnauthorizedRequestException {
        //set up
        mockSingletons();
        try {
            //exercise
            federatedNetworkOrderController.getFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: Tests if a delete operation deletes federatedNetwork from activeFederatedNetworks.
    @Test
    public void testDeleteFederatedNetwork() throws FederatedNetworkNotFoundException, AgentCommucationException,
            SQLException, UnauthorizedRequestException {
        //set up
        mockOnlyDatabase();
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        when(federatedNetwork.getUser()).thenReturn(user);
        when(federatedNetwork.getId()).thenReturn(FEDERATED_NETWORK_ID);
        federatedNetworkOrdersHolder.putOrder(federatedNetwork);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.deleteFederatedNetwork(anyString())).willReturn(true);
        try {
            //exercise
            federatedNetworkOrderController.deleteFederatedNetwork(FEDERATED_NETWORK_ID, user);
            //verify
        } catch (NotEmptyFederatedNetworkException e) {
            fail();
        }
        try {
            //exercise
            FederatedNetworkOrder returnedOrder = federatedNetworkOrderController.getFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
        verify(federatedNetwork, times(1)).setOrderState(OrderState.DEACTIVATED);
        assertNull(federatedNetworkOrdersHolder.getFederatedNetworkOrder(FEDERATED_NETWORK_ID));
    }

    //test case: This test check if a delete in nonexistent federatedNetwork will throw a FederatedNetworkNotFoundException
    @Test
    public void testDeleteNonExistentFederatedNetwork() throws NotEmptyFederatedNetworkException,
            AgentCommucationException, UnauthorizedRequestException {
        //set up
        mockSingletons();
        try {
            //exercise
            federatedNetworkOrderController.deleteFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: This test check if an error communicating with agent will throw an AgentCommucationException
    @Test
    public void testErrorInAgentCommunication() throws FederatedNetworkNotFoundException,
            NotEmptyFederatedNetworkException, UnauthorizedRequestException {
        //set up
        mockSingletons();
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        federatedNetwork.setId(FEDERATED_NETWORK_ID);
        when(federatedNetwork.getUser()).thenReturn(user);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetworkOrdersHolder.getOrder(FEDERATED_NETWORK_ID)).thenReturn(federatedNetwork);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.deleteFederatedNetwork(anyString())).willReturn(false);
        try {
            //exercise
            federatedNetworkOrderController.deleteFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (AgentCommucationException e) {
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
        when(federatedNetwork.getUser()).thenReturn(user);
        when(federatedNetwork2.getUser()).thenReturn(user);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetwork2.getOrderState()).thenReturn(OrderState.FULFILLED);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        fakeActiveFederatedNetworks.put(federatedNetworkId2, federatedNetwork2);
        BDDMockito.given(federatedNetworkOrdersHolder.getActiveOrdersMap()).willReturn(fakeActiveFederatedNetworks);
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(federatedNetworkOrderController.getUserFederatedNetworksStatus(user));
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
        FederationUserToken nonAuthenticatedUser = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, nonAuthenticatedUserId, USER_NAME);
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        FederatedNetworkOrder federatedNetwork2 = mock(FederatedNetworkOrder.class);
        String federatedNetworkId2 = FEDERATED_NETWORK_ID + 2;
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getId()).thenReturn(FEDERATED_NETWORK_ID);
        when(federatedNetwork2.getId()).thenReturn(federatedNetworkId2);
        when(federatedNetwork.getUser()).thenReturn(user);
        when(federatedNetwork2.getUser()).thenReturn(nonAuthenticatedUser);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetwork2.getOrderState()).thenReturn(OrderState.FULFILLED);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        fakeActiveFederatedNetworks.put(federatedNetworkId2, federatedNetwork2);
        BDDMockito.given(federatedNetworkOrdersHolder.getActiveOrdersMap()).willReturn(fakeActiveFederatedNetworks);
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(federatedNetworkOrderController.getUserFederatedNetworksStatus(user));
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
        List<InstanceStatus> federatedNetworks = new ArrayList<>(federatedNetworkOrderController.getUserFederatedNetworksStatus(user));
        //verify
        assertEquals(0, federatedNetworks.size());
    }


    // compute tests

    //test case: Tests if to add a new federated compute, federatedNetworkOrderController makes the correct calls to the collaborators.
    @Test
    public void testAddFederatedCompute() {
        //set up
        mockOnlyDatabase();
        String cidr = "10.10.10.0/24";
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, user, MEMBER,
                MEMBER, cidr, "test", allowedMembers, freedIps, computesIp));
        federatedNetworkOrdersHolder.putOrder(federatedNetwork);

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

    //test case: This test expects a FederatedNetworkException, since will be given a nonexistent federatedNetwork id
    @Test
    public void testAddComputeFederatedWithNonexistentNetwork() {
        //set up
        mockSingletons();
        String nonexistentId = "nonexistent-id";
//        ComputeOrder computeOrder = new ComputeOrder();
//        computeOrder.setId(FEDERATED_COMPUTE_ID);
//        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(nonexistentId, "", computeOrder));
//        try {
//            //exercise
//            federatedNetworkOrderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
//            fail();
//        } catch (FederatedNetworkNotFoundException e) {
//            //verify
//        }
    }

    //test case: Tests if get all in an empty federated networks list will return the same computeOrder given as input.
    @Test
    public void testAddComputeNotFederated() {
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
        ComputeInstance computeInstance = new ComputeInstance(FEDERATED_COMPUTE_ID, org.fogbowcloud.ras.core.models.instances.InstanceState.READY, "host",
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
        FederationUserToken nonAuthenticatedUser = new FederationUserToken(MEMBER, TOKEN_USER_VALUE, nonAuthenticatedUserId, USER_NAME);
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
//        Collection<FederatedNetworkOrder> orders = federatedNetworkOrdersHolder.getActiveOrdersMap().values();
//        Map<String, FederatedNetworkOrder> federatedNetworks = getFederatedNetworksMap(orders);
//        Map<String, FederatedComputeOrder> federatedComputes = getFederatedComputesMap(orders);
//        FederatedNetworkOrder federatedNetwork = federatedNetworkOrdersHolder.getFederatedNetworkOrder(FEDERATED_NETWORK_ID);
//        federatedNetwork.setUser(user);
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
        Collection<FederatedNetworkOrder> orders = federatedNetworkOrdersHolder.getActiveOrdersMap().values();
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
    public void testRoolbackInAFailedCompute() {
        //set up
        mockOnlyDatabase();
        addNetworkIntoActiveOrdersMap();
//        ComputeOrder computeOrder = new ComputeOrder();
//        computeOrder.setId(FEDERATED_COMPUTE_ID);
//        computeOrder.setFederationUserToken(user);
//        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));
//        federatedNetworkOrderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
//
//        Collection<FederatedNetworkOrder> orders = federatedNetworkOrdersHolder.getActiveOrdersMap().values();
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

    private void addComputeIntoActiveOrdersMap() {
//        ComputeOrder computeOrder = new ComputeOrder();
//        computeOrder.setId(FEDERATED_COMPUTE_ID);
//        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));
//        federatedCompute.setUser(user);
//        when(federatedCompute.getId()).thenReturn(FEDERATED_COMPUTE_ID);
//        when(federatedCompute.getUser()).thenReturn(user);
//        try {
//            federatedNetworkOrdersHolder.getInstance().putOrder(federatedCompute);
//        } catch (Exception e) {
//            fail();
//        }
    }

    private void addNetworkIntoActiveOrdersMap() {
        String cidr = "10.10.10.0/24";
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, user, MEMBER,
                MEMBER, cidr, "test", allowedMembers, freedIps, computesIp));
        try {
            federatedNetworkOrdersHolder.getInstance().putOrder(federatedNetwork);
        } catch (Exception e) {
            fail();
        }
    }



    private Map<String, FederatedNetworkOrder> getFederatedNetworksMap(Collection<FederatedNetworkOrder> activeOrdersMap) {
        return activeOrdersMap.stream()
                .map(order -> order)
                .collect(Collectors.toMap(order -> order.getId(), order -> order));
    }
}
