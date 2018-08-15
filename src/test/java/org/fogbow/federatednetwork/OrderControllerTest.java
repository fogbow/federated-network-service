package org.fogbow.federatednetwork;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.exceptions.AgentCommucationException;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.utils.AgentCommunicatorUtil;
import org.fogbow.federatednetwork.utils.FederatedNetworkUtil;
import org.fogbowcloud.manager.core.exceptions.InvalidParameterException;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
        federatedNetwork.setId(federatedNetworkId);
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


}
