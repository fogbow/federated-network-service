package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.model.FederatedComputeInstance;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.utils.AgentCommunicatorUtil;
import org.fogbow.federatednetwork.utils.FederateComputeUtil;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbowcloud.manager.core.exceptions.InvalidParameterException;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.manager.core.models.InstanceStatus;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.instances.InstanceState;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;
import org.fogbowcloud.manager.core.plugins.cloud.util.CloudInitUserDataBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AgentCommunicatorUtil.class, FederatedNetworkUtil.class, FederateComputeUtil.class, DatabaseManager.class})
public class OrderControllerTest extends BaseUnitTest {

    private final String FEDERATED_NETWORK_ID = "fake-network-id";
    private final String FEDERATED_COMPUTE_ID = "fake-compute-id";
    private final String USER_ID = "fake-user-id";
    private final String USER_NAME = "fake-user-name";
    private final String TOKEN_PROVIDER = "token-provider";
    private final String TOKEN_USER_VALUE = "token-value";

    Properties properties;
    FederationUserToken user;
    OrderController orderController;

    @Before
    public void setUp() throws InvalidParameterException {
        properties = super.setProperties();
        orderController = spy(new OrderController(properties));
        user = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, USER_ID, USER_NAME);
    }

    //test case: Tests if the activation order made in orderController will call the expected methods
    @Test
    public void testActivatingFederatedNetwork() throws InvalidCidrException, AgentCommucationException {
        //set up
        String fakeCidr = "10.10.10.0/24";
        SubnetUtils.SubnetInfo fakeSubnetInfo = new SubnetUtils(fakeCidr).getInfo();
        FederationUserToken user = mock(FederationUserToken.class);
        FederatedNetworkOrder federatedNetworkOrder = spy(new FederatedNetworkOrder());
        federatedNetworkOrder.setId(FEDERATED_NETWORK_ID);
        federatedNetworkOrder.setCidrNotation(fakeCidr);
        doNothing().when(federatedNetworkOrder).setCachedInstanceState(InstanceState.READY);
        doNothing().when(federatedNetworkOrder).setOrderState(OrderState.FULFILLED);
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        BDDMockito.given(FederatedNetworkUtil.getSubnetInfo(anyString())).willReturn(fakeSubnetInfo);
        BDDMockito.given(FederatedNetworkUtil.isSubnetValid(any(SubnetUtils.SubnetInfo.class))).willReturn(true);
        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.createFederatedNetwork(anyString(), anyString(), any(Properties.class))).willReturn(true);
        // exercise
        String returnedId = orderController.activateFederatedNetwork(federatedNetworkOrder, user);
        //verify
        verify(federatedNetworkOrder, times(1)).setCachedInstanceState(InstanceState.READY);
        verify(federatedNetworkOrder, times(1)).setOrderState(OrderState.FULFILLED);
        assertEquals(FEDERATED_NETWORK_ID, returnedId);
    }

    //test case: Tests if any error in communication with agent, will throw an exception
    @Test
    public void testAgentCommunicationError() throws InvalidCidrException {
        //set up
        String fakeCidr = "10.10.10.0/24";
        SubnetUtils.SubnetInfo fakeSubnetInfo = new SubnetUtils(fakeCidr).getInfo();
        FederationUserToken user = mock(FederationUserToken.class);
        FederatedNetworkOrder federatedNetworkOrder = spy(new FederatedNetworkOrder());
        federatedNetworkOrder.setId(FEDERATED_NETWORK_ID);
        federatedNetworkOrder.setCidrNotation(fakeCidr);
        doNothing().when(federatedNetworkOrder).setCachedInstanceState(InstanceState.READY);
        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        BDDMockito.given(FederatedNetworkUtil.getSubnetInfo(anyString())).willReturn(fakeSubnetInfo);
        BDDMockito.given(FederatedNetworkUtil.isSubnetValid(any(SubnetUtils.SubnetInfo.class))).willReturn(true);
        doNothing().when(federatedNetworkOrder).setOrderState(OrderState.FULFILLED);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.createFederatedNetwork(anyString(), anyString(), any(Properties.class))).willReturn(false);
        // exercise
        try {
            orderController.activateFederatedNetwork(federatedNetworkOrder, user);
            fail();
        } catch (AgentCommucationException e) {
            //verify
            verify(federatedNetworkOrder, times(0)).setCachedInstanceState(InstanceState.READY);
            verify(federatedNetworkOrder, times(0)).setOrderState(OrderState.FULFILLED);
        }
    }

    //test case: Tests that can retrieve a federated network stored into activeFederatedNetwork.
    @Test
    public void testGetFederatedNetwork() throws UnauthenticatedUserException {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getFederationUserToken()).thenReturn(user);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);
        //exercise
        try {
            FederatedNetworkOrder returnedOrder = orderController.getFederatedNetwork(FEDERATED_NETWORK_ID, user);
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
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getFederationUserToken()).thenReturn(user);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);

        String nonAuthenticatedUserId = "non-autheticated";
        FederationUserToken nonAuthenticatedUser = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, nonAuthenticatedUserId, USER_NAME);
        //exercise
        try {
            orderController.getFederatedNetwork(FEDERATED_NETWORK_ID, nonAuthenticatedUser);
            fail();
        } catch (UnauthenticatedUserException e) {
            //verify
        }
    }

    //test case: This test check if a federated network that can't be found, this get operation should throw a FederatedNetworkNotFoundException
    @Test
    public void testGetNotExistentFederatedNetwork() throws UnauthenticatedUserException {
        //exercise
        try {
            orderController.getFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: Tests if a delete operation deletes federatedNetwork from activeFederatedNetworks.
    @Test
    public void testDeleteFederatedNetwork() throws FederatedNetworkNotFoundException, AgentCommucationException, UnauthenticatedUserException {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getFederationUserToken()).thenReturn(user);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.deleteFederatedNetwork(anyString(), any(Properties.class))).willReturn(true);
        try {
            //exercise
            orderController.deleteFederatedNetwork(FEDERATED_NETWORK_ID, user);
            //verify
        } catch (NotEmptyFederatedNetworkException e) {
            fail();
        }
        try {
            //exercise
            FederatedNetworkOrder returnedOrder = orderController.getFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
        verify(federatedNetwork, times(1)).setOrderState(OrderState.DEACTIVATED);
        assertNull(orderController.getActiveFederatedNetworks().get(FEDERATED_NETWORK_ID));
    }

    //test case: This test check if a delete in nonexistent federatedNetwork will throw a FederatedNetworkNotFoundException
    @Test
    public void testDeleteNonExistentFederatedNetwork() throws NotEmptyFederatedNetworkException, AgentCommucationException, UnauthenticatedUserException {
        //set up
        try {
            //exercise
            orderController.deleteFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: This test check if an error communicating with agent will throw an AgentCommucationException
    @Test
    public void testErrorInAgentCommunication() throws FederatedNetworkNotFoundException, NotEmptyFederatedNetworkException, UnauthenticatedUserException {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        federatedNetwork.setId(FEDERATED_NETWORK_ID);
        when(federatedNetwork.getFederationUserToken()).thenReturn(user);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.deleteFederatedNetwork(anyString(), any(Properties.class))).willReturn(false);
        try {
            //exercise
            orderController.deleteFederatedNetwork(FEDERATED_NETWORK_ID, user);
            fail();
        } catch (AgentCommucationException e) {
            //verify
        }
    }

    //test case: Tests if get all federated networks will return all federated networks added
    @Test
    public void testGetFederatedNetworks() {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        FederatedNetworkOrder federatedNetwork2 = mock(FederatedNetworkOrder.class);
        String federatedNetworkId2 = FEDERATED_NETWORK_ID + 2;
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getId()).thenReturn(FEDERATED_NETWORK_ID);
        when(federatedNetwork2.getId()).thenReturn(federatedNetworkId2);
        when(federatedNetwork.getFederationUserToken()).thenReturn(user);
        when(federatedNetwork2.getFederationUserToken()).thenReturn(user);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetwork2.getOrderState()).thenReturn(OrderState.FULFILLED);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        fakeActiveFederatedNetworks.put(federatedNetworkId2, federatedNetwork2);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(orderController.getUserFederatedNetworksStatus(user));
        //verify
        assertEquals(2, federatedNetworks.size());
        assertEquals(FEDERATED_NETWORK_ID, federatedNetworks.get(0).getInstanceId());
        assertEquals(federatedNetworkId2, federatedNetworks.get(1).getInstanceId());
    }

    //test case: Tests if get all federated networks will return only one federated network, since the other one was
    // activated by another user
    @Test
    public void testGetFederatedNetworksWithDifferentUser() throws InvalidParameterException {
        //set up
        String nonAuthenticatedUserId = "non-authenticated";
        FederationUserToken nonAuthenticatedUser = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, nonAuthenticatedUserId, USER_NAME);
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        FederatedNetworkOrder federatedNetwork2 = mock(FederatedNetworkOrder.class);
        String federatedNetworkId2 = FEDERATED_NETWORK_ID + 2;
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getId()).thenReturn(FEDERATED_NETWORK_ID);
        when(federatedNetwork2.getId()).thenReturn(federatedNetworkId2);
        when(federatedNetwork.getFederationUserToken()).thenReturn(user);
        when(federatedNetwork2.getFederationUserToken()).thenReturn(nonAuthenticatedUser);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetwork2.getOrderState()).thenReturn(OrderState.FULFILLED);
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        fakeActiveFederatedNetworks.put(federatedNetworkId2, federatedNetwork2);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(orderController.getUserFederatedNetworksStatus(user));
        //verify
        assertEquals(1, federatedNetworks.size());
        assertEquals(FEDERATED_NETWORK_ID, federatedNetworks.get(0).getInstanceId());
    }

    //test case: Tests if get all in an Empty federated networks list will return correctly
    @Test
    public void testGetEmptyListOfFederatedNetworks() {
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(orderController.getUserFederatedNetworksStatus(user));
        //verify
        assertEquals(0, federatedNetworks.size());
    }


    // compute tests

    //test case: Tests if to add a new federated compute, orderController makes the correct calls to the collaborators.
    @Test
    public void testAddFederatedCompute() throws FederatedNetworkNotFoundException, InvalidCidrException,
            SubnetAddressesCapacityReachedException, IOException {
        //set up
        String cidr = "10.10.10.0/24";
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, user, cidr, "test",
                allowedMembers, 1, freedIps, computesIp));
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        fakeActiveFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);

        String federatedIp = "10.10.10.2";
        ComputeOrder computeOrder = new ComputeOrder();
        computeOrder.setId(FEDERATED_COMPUTE_ID);
        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));

        PowerMockito.mockStatic(FederatedNetworkUtil.class);
        BDDMockito.given(FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork)).willReturn(federatedIp);
        PowerMockito.mockStatic(FederateComputeUtil.class);
        UserData fakeUserData = new UserData("", CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
        BDDMockito.given(FederateComputeUtil.addUserData(any(ComputeOrder.class), anyString(), anyString(), anyString(),
                anyString())).willReturn(createComputeWithUserData(computeOrder, fakeUserData));
        //exercise
        orderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
        //verify
        verify(federatedCompute, times(1)).setFederatedIp(federatedIp);
        PowerMockito.verifyStatic(FederatedNetworkUtil.class, times(1));
        FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
        PowerMockito.verifyStatic(FederateComputeUtil.class, times(1));
        FederateComputeUtil.addUserData(any(ComputeOrder.class), anyString(), anyString(), anyString(),
                anyString());
        assertNotEquals(computeOrder.getUserData(), orderController.getActiveFederatedNetworks().get(FEDERATED_NETWORK_ID));
    }

    //test case: This test expects a FederatedNetworkException, since will be given a nonexistent federatedNetwork id
    @Test
    public void testAddComputeFederatedWithNonexistentNetwork() throws InvalidCidrException,
            SubnetAddressesCapacityReachedException, IOException {
        //set up
        String nonexistentId = "nonexistent-id";
        ComputeOrder computeOrder = new ComputeOrder();
        computeOrder.setId(FEDERATED_COMPUTE_ID);
        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(nonexistentId, "", computeOrder));
        try {
            //exercise
            orderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: Tests if get all in an empty federated networks list will return the same computeOrder given as input.
    @Test
    public void testAddComputeNotFederated() throws InvalidCidrException, SubnetAddressesCapacityReachedException,
            FederatedNetworkNotFoundException, IOException {
        //set up
        ComputeOrder computeOrder = new ComputeOrder();
        computeOrder.setId(FEDERATED_COMPUTE_ID);
        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder("", "", computeOrder));
        //exercise
        ComputeOrder computeReturned = orderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
        //verify
        assertEquals(computeOrder, computeReturned);
    }

    //test case: Tests if updates correctly the compute with the new id
    @Test
    public void testUpdateFederatedComputeId() {
        //set up
        String newId = "fake-compute-new-id";
        ComputeOrder computeOrder = new ComputeOrder();
        computeOrder.setId(FEDERATED_COMPUTE_ID);
        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_COMPUTE_ID, FEDERATED_NETWORK_ID, computeOrder));

        DatabaseManager database = Mockito.mock(DatabaseManager.class);
        PowerMockito.mockStatic(DatabaseManager.class);
        Mockito.doNothing().when(database).putFederatedCompute(any(FederatedComputeOrder.class), any(FederationUserToken.class));
        BDDMockito.given(DatabaseManager.getInstance()).willReturn(database);

        assertEquals(FEDERATED_COMPUTE_ID, federatedCompute.getComputeOrder().getId());
        assertNotEquals(newId, federatedCompute.getComputeOrder().getId());
        //exercise
        orderController.updateIdOnComputeCreation(federatedCompute, newId);
        //verify
        assertNotEquals(FEDERATED_COMPUTE_ID, federatedCompute.getComputeOrder().getId());
        assertEquals(newId, federatedCompute.getComputeOrder().getId());
    }

    //test case: This test should add federated data to federated compute when receives a get method.
    @Test
    public void testGetFederatedCompute() throws UnauthenticatedUserException {
        //set up
        addNetworkIntoActiveMaps();
        addComputeIntoActiveMaps();
        ComputeInstance computeInstance = new ComputeInstance(FEDERATED_COMPUTE_ID, InstanceState.READY, "host", 2, 8, 20, "192.168.0.2");
        //exercise
        ComputeInstance federatedComputeInstance = orderController.addFederatedIpInGetInstanceIfApplied(computeInstance, user);
        //verify
        assertNotEquals(computeInstance, federatedComputeInstance);
        assertTrue(federatedComputeInstance instanceof FederatedComputeInstance);
        assertNotNull(((FederatedComputeInstance) federatedComputeInstance).getFederatedIp());
    }

    //test case: This test should throw an UnauthenticatedUserException since, a different user is trying to access it.
    @Test
    public void testGetFederatedComputeWithNonAuthenticatedUser() throws InvalidParameterException {
        //set up
        addNetworkIntoActiveMaps();
        addComputeIntoActiveMaps();
        ComputeInstance computeInstance = new ComputeInstance(FEDERATED_COMPUTE_ID);
        String nonAuthenticatedUserId = "non-authenticated";
        FederationUserToken nonAuthenticatedUser = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, nonAuthenticatedUserId, USER_NAME);
        //exercise
        try {
            orderController.addFederatedIpInGetInstanceIfApplied(computeInstance, nonAuthenticatedUser);
            fail();
        } catch (UnauthenticatedUserException e) {
            //verify
        }
    }

    //test case: A get method to a not federated compute should return exactly the same computeInstance, since it's not a federated compute.
    @Test
    public void testGetNotFederatedCompute() throws UnauthenticatedUserException {
        //set up
        ComputeInstance computeInstance = new ComputeInstance(FEDERATED_COMPUTE_ID, InstanceState.READY, "host", 2, 8, 20, "192.168.0.2");
        //exercise
        ComputeInstance federatedComputeInstance = orderController.addFederatedIpInGetInstanceIfApplied(computeInstance, user);
        //verify
        assertEquals(computeInstance, federatedComputeInstance);
        assertFalse(federatedComputeInstance instanceof FederatedComputeInstance);
    }

    //test case: This test should remove a compute order.
    @Test
    public void testDeleteFederatedCompute() throws FederatedNetworkNotFoundException,
            UnauthenticatedUserException, FederatedComputeNotFoundException, InvalidCidrException, SubnetAddressesCapacityReachedException, IOException {
        //set up
        addNetworkIntoActiveMaps();
        ComputeOrder computeOrder = new ComputeOrder();
        computeOrder.setId(FEDERATED_COMPUTE_ID);
        computeOrder.setFederationUserToken(user);
        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));
        orderController.addFederationUserTokenDataIfApplied(federatedCompute, user);
        orderController.updateIdOnComputeCreation(federatedCompute, FEDERATED_COMPUTE_ID);
        //pre conditions
        Map<String, FederatedComputeOrder> federatedComputes = orderController.getActiveFederatedComputes();
        Map<String, FederatedNetworkOrder> federatedNetworks = orderController.getActiveFederatedNetworks();
        FederatedNetworkOrder federatedNetwork = federatedNetworks.get(FEDERATED_NETWORK_ID);
        String computeIp = "10.10.10.1";
        assertEquals(1, federatedNetworks.size());
        assertNotNull(federatedComputes.get(FEDERATED_COMPUTE_ID));
        assertTrue(federatedNetwork.getFreedIps().isEmpty());
        assertEquals(2, federatedNetwork.getIpsServed());
        assertFalse(federatedNetwork.getComputesIp().isEmpty());
        //exercise
        orderController.deleteCompute(FEDERATED_COMPUTE_ID, user);
        //verify
        federatedComputes = orderController.getActiveFederatedComputes();
        federatedNetworks = orderController.getActiveFederatedNetworks();
        federatedNetwork = federatedNetworks.get(FEDERATED_NETWORK_ID);
        assertEquals(1, federatedNetworks.size());
        assertNull(federatedComputes.get(FEDERATED_COMPUTE_ID));
        assertFalse(federatedNetwork.getFreedIps().isEmpty());
        assertEquals(2, federatedNetwork.getIpsServed());
        assertTrue(federatedNetwork.getComputesIp().isEmpty());
    }

    //test case: A delete in a nonexistent federated compute
    @Test
    public void testRemoveNonFederatedCompute() throws UnauthenticatedUserException, FederatedNetworkNotFoundException {
        //set up
        addNetworkIntoActiveMaps();
        Map<String, FederatedNetworkOrder> federatedNetworks = orderController.getActiveFederatedNetworks();
        FederatedNetworkOrder federatedNetworkOrder = federatedNetworks.get(FEDERATED_NETWORK_ID);
        //exercise
        orderController.deleteCompute(FEDERATED_COMPUTE_ID, user);
        //verify
        verify(federatedNetworkOrder, never()).removeAssociatedIp(anyString());
    }

    //test case: A delete with a different user must raise an UnauthenticatedUserException.
    @Test
    public void testRemoveFederatedComputeWithDifferentUser() throws FederatedNetworkNotFoundException, InvalidParameterException {
        //set up
        addNetworkIntoActiveMaps();
        addComputeIntoActiveMaps();
        //exercise
        try {
            String nonAuthenticatedUserId = "non-authenticated";
            FederationUserToken nonAuthenticatedUser = new FederationUserToken(TOKEN_PROVIDER, TOKEN_USER_VALUE, nonAuthenticatedUserId, USER_NAME);
            orderController.deleteCompute(FEDERATED_COMPUTE_ID, nonAuthenticatedUser);
            fail();
        } catch (UnauthenticatedUserException e) {
            //verify
        }
    }

    //test case: Tests rollback in a computeOrder, in case of failing to communicate with resource allocation service.
    @Test
    public void testRoolbackInAFailedCompute() throws FederatedNetworkNotFoundException, InvalidCidrException,
            SubnetAddressesCapacityReachedException, IOException {
        //set up
        addNetworkIntoActiveMaps();
        ComputeOrder computeOrder = new ComputeOrder();
        computeOrder.setId(FEDERATED_COMPUTE_ID);
        computeOrder.setFederationUserToken(user);
        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));
        orderController.addFederationUserTokenDataIfApplied(federatedCompute, user);

        Map<String, FederatedNetworkOrder> federatedNetworks = orderController.getActiveFederatedNetworks();
        FederatedNetworkOrder federatedNetworkOrder = federatedNetworks.get(FEDERATED_NETWORK_ID);

        //pre conditions
        assertFalse(federatedNetworkOrder.getComputesIp().isEmpty());
        assertEquals(2, federatedNetworkOrder.getIpsServed());
        assertTrue(federatedNetworkOrder.getFreedIps().isEmpty());
        //exercise
        orderController.rollbackInFailedPost(federatedCompute);
        //verify
        verify(federatedNetworkOrder, times(1)).removeAssociatedIp(anyString());
        assertTrue(federatedNetworkOrder.getComputesIp().isEmpty());
        assertEquals(2, federatedNetworkOrder.getIpsServed());
        assertFalse(federatedNetworkOrder.getFreedIps().isEmpty());
        assertEquals(federatedCompute.getFederatedIp(), federatedNetworkOrder.getFreedIps().element());
    }

    private void addComputeIntoActiveMaps() {
        ComputeOrder computeOrder = new ComputeOrder();
        computeOrder.setId(FEDERATED_COMPUTE_ID);
        computeOrder.setFederationUserToken(user);
        FederatedComputeOrder federatedCompute = spy(new FederatedComputeOrder(FEDERATED_NETWORK_ID, "", computeOrder));
        Map<String, FederatedComputeOrder> activeFederatedCompute = new ConcurrentHashMap<>();
        activeFederatedCompute.put(FEDERATED_COMPUTE_ID, federatedCompute);
        orderController.setActiveFederatedComputes(activeFederatedCompute);
    }

    private void addNetworkIntoActiveMaps() {
        String cidr = "10.10.10.0/24";
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(FEDERATED_NETWORK_ID, user, cidr, "test",
                allowedMembers, 1, freedIps, computesIp));
        Map<String, FederatedNetworkOrder> activeFederatedNetworks = new ConcurrentHashMap<>();
        activeFederatedNetworks.put(FEDERATED_NETWORK_ID, federatedNetwork);
        orderController.setActiveFederatedNetworks(activeFederatedNetworks);

        DatabaseManager database = Mockito.mock(DatabaseManager.class);
        PowerMockito.mockStatic(DatabaseManager.class);
        Mockito.doNothing().when(database).putFederatedNetwork(any(FederatedNetworkOrder.class), any(FederationUserToken.class));
        BDDMockito.given(DatabaseManager.getInstance()).willReturn(database);
    }

    private static ComputeOrder createComputeWithUserData(ComputeOrder computeOrder, UserData userData) {
        ComputeOrder newCompute = new ComputeOrder(computeOrder.getId(), computeOrder.getFederationUserToken(),
                computeOrder.getRequestingMember(), computeOrder.getProvidingMember(), computeOrder.getvCPU(),
                computeOrder.getMemory(), computeOrder.getDisk(), computeOrder.getImageId(),
                userData, computeOrder.getPublicKey(), computeOrder.getNetworksId());
        return newCompute;
    }
}
