package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.exceptions.AgentCommucationException;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.utils.AgentCommunicatorUtil;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbowcloud.manager.core.exceptions.InvalidParameterException;
import org.fogbowcloud.manager.core.models.InstanceStatus;
import org.fogbowcloud.manager.core.models.instances.InstanceState;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AgentCommunicatorUtil.class, FederatedNetworkUtil.class})
public class OrderControllerTest extends BaseUnitTest {

    Properties properties;
    String federatedNetworkId;
    FederationUser user;
    OrderController orderController;

    @Before
    public void setUp() throws InvalidParameterException {
        properties = super.setProperties();
        orderController = spy(new OrderController(properties));
        federatedNetworkId = "fake-id";
        Map<String, String> fedUserAttrs = new HashMap<>();
        fedUserAttrs.put(FederationUser.MANDATORY_NAME_ATTRIBUTE, "fake-name");
        user = new FederationUser("fake-user-id", fedUserAttrs);
    }

    //test case: Tests if the activation order made in orderController will call the expected methods
    @Test
    public void testActivatingFederatedNetwork() throws InvalidCidrException, AgentCommucationException {
        //set up
        String fakeCidr = "10.10.10.0/24";
        SubnetUtils.SubnetInfo fakeSubnetInfo = new SubnetUtils(fakeCidr).getInfo();
        FederationUser user = mock(FederationUser.class);
        FederatedNetworkOrder federatedNetworkOrder = spy(new FederatedNetworkOrder());
        federatedNetworkOrder.setId(federatedNetworkId);
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
        assertEquals(federatedNetworkId, returnedId);
    }

    //test case: Tests if any error in communication with agent, will throw an exception
    @Test
    public void testAgentCommunicationError() throws InvalidCidrException {
        //set up
        String fakeCidr = "10.10.10.0/24";
        SubnetUtils.SubnetInfo fakeSubnetInfo = new SubnetUtils(fakeCidr).getInfo();
        FederationUser user = mock(FederationUser.class);
        FederatedNetworkOrder federatedNetworkOrder = spy(new FederatedNetworkOrder());
        federatedNetworkOrder.setId(federatedNetworkId);
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
    public void testGetFederatedNetwork() {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getFederationUser()).thenReturn(user);
        fakeActiveFederatedNetworks.put(federatedNetworkId, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);
        //exercise
        try {
            FederatedNetworkOrder returnedOrder = orderController.getFederatedNetwork(federatedNetworkId, user);
            //verify
            assertEquals(federatedNetwork, returnedOrder);
        } catch (FederatedNetworkNotFoundException e) {
            fail();
        }
    }

    //test case: This test check if a federated network that can't be found, this get operation should throw a FederatedNetworkNotFoundException
    @Test
    public void testGetNotExistentFederatedNetwork() {
        //exercise
        try {
            FederatedNetworkOrder returnedOrder = orderController.getFederatedNetwork(federatedNetworkId, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: Tests if a delete operation deletes federatedNetwork from activeFederatedNetworks.
    @Test
    public void testDeleteFederatedNetwork() throws FederatedNetworkNotFoundException, AgentCommucationException {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getFederationUser()).thenReturn(user);
        fakeActiveFederatedNetworks.put(federatedNetworkId, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.deleteFederatedNetwork(anyString(), any(Properties.class))).willReturn(true);
        try {
            //exercise
            orderController.deleteFederatedNetwork(federatedNetworkId, user);
            //verify
        } catch (NotEmptyFederatedNetworkException e) {
            fail();
        }
        try {
            //exercise
            FederatedNetworkOrder returnedOrder = orderController.getFederatedNetwork(federatedNetworkId, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
        verify(federatedNetwork, times(1)).setOrderState(OrderState.DEACTIVATED);
        assertNull(orderController.getActiveFederatedNetworks().get(federatedNetworkId));
    }

    //test case: This test check if a delete in nonexistent federatedNetwork will throw a FederatedNetworkNotFoundException
    @Test
    public void testDeleteNonExistentFederatedNetwork() throws NotEmptyFederatedNetworkException, AgentCommucationException {
        //set up
        try {
            //exercise
            orderController.deleteFederatedNetwork(federatedNetworkId, user);
            fail();
        } catch (FederatedNetworkNotFoundException e) {
            //verify
        }
    }

    //test case: This test check if an error communicating with agent will throw an AgentCommucationException
    @Test
    public void testErrorInAgentCommunication() throws FederatedNetworkNotFoundException, NotEmptyFederatedNetworkException {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        federatedNetwork.setId(federatedNetworkId);
        when(federatedNetwork.getFederationUser()).thenReturn(user);
        fakeActiveFederatedNetworks.put(federatedNetworkId, federatedNetwork);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);

        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        BDDMockito.given(AgentCommunicatorUtil.deleteFederatedNetwork(anyString(), any(Properties.class))).willReturn(false);
        try {
            //exercise
            orderController.deleteFederatedNetwork(federatedNetworkId, user);
            fail();
        } catch (AgentCommucationException e) {
            //verify
        }
    }

    //test case: Tests if get all federated networks will return correctly
    @Test
    public void testGetFederatedNetworks() {
        //set up
        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
        FederatedNetworkOrder federatedNetwork2 = mock(FederatedNetworkOrder.class);
        String federatedNetworkId2 = federatedNetworkId + 2;
        Map<String, FederatedNetworkOrder> fakeActiveFederatedNetworks = new ConcurrentHashMap<>();
        when(federatedNetwork.getId()).thenReturn(federatedNetworkId);
        when(federatedNetwork2.getId()).thenReturn(federatedNetworkId2);
        when(federatedNetwork.getFederationUser()).thenReturn(user);
        when(federatedNetwork2.getFederationUser()).thenReturn(user);
        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
        when(federatedNetwork2.getOrderState()).thenReturn(OrderState.FULFILLED);
        fakeActiveFederatedNetworks.put(federatedNetworkId, federatedNetwork);
        fakeActiveFederatedNetworks.put(federatedNetworkId2, federatedNetwork2);
        orderController.setActiveFederatedNetworks(fakeActiveFederatedNetworks);
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(orderController.getUserFederatedNetworksStatus(user));
        //verify
        assertEquals(2, federatedNetworks.size());
        assertEquals(federatedNetworkId, federatedNetworks.get(0).getInstanceId());
        assertEquals(federatedNetworkId2, federatedNetworks.get(1).getInstanceId());
    }

    //test case: Tests if get all in an Empty federated networks list will return correctly
    @Test
    public void testGetEmptyListOfFederatedNetworks() {
        //exercise
        List<InstanceStatus> federatedNetworks = new ArrayList<>(orderController.getUserFederatedNetworksStatus(user));
        //verify
        assertEquals(0, federatedNetworks.size());
    }


}
