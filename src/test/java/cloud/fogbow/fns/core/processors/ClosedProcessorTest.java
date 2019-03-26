package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.exceptions.AgentCommucationException;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

public class ClosedProcessorTest {
    // TODO ARNETT Adapt these tests
//    //test case: Tests if any error in communication with agent, will lead the order to fail
//    @Test
//    public void testAgentCommunicationError() throws InvalidCidrException, UnexpectedException {
//        //set up
//        mockSingletons();
//        String fakeCidr = "10.10.10.0/24";
//        SubnetUtils.SubnetInfo fakeSubnetInfo = new SubnetUtils(fakeCidr).getInfo();
//        SystemUser user = Mockito.mock(SystemUser.class);
//        FederatedNetworkOrder federatedNetworkOrder = spy(new FederatedNetworkOrder());
//        federatedNetworkOrder.setId(FEDERATED_NETWORK_ID);
//        federatedNetworkOrder.setCidr(fakeCidr);
//        PowerMockito.mockStatic(FederatedNetworkUtil.class);
//        BDDMockito.given(FederatedNetworkUtil.getSubnetInfo(anyString())).willReturn(fakeSubnetInfo);
//        BDDMockito.given(FederatedNetworkUtil.isSubnetValid(any(SubnetUtils.SubnetInfo.class))).willReturn(true);
//        doNothing().when(federatedNetworkOrder).setOrderState(OrderState.FULFILLED);
//
//        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
//        BDDMockito.given(AgentCommunicatorUtil.createFederatedNetwork(anyString(), anyString())).willReturn(false);
//        // exercise
//        federatedNetworkOrderController.addFederatedNetwork(federatedNetworkOrder, user);
//        assertEquals(OrderState.FAILED, federatedNetworkOrder.getOrderState());
//    }
//
//    @Test(expected = AgentCommucationException.class)
//    public void testFailureWhileDeletingFederatedNetwork() throws AgentCommucationException, FederatedNetworkNotFoundException,
//            UnauthorizedRequestException, NotEmptyFederatedNetworkException, UnexpectedException {
//        // set up
//        mockDatabase(new HashMap<>());
//        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
//        Mockito.when(AgentCommunicatorUtil.deleteFederatedNetwork(federatedNetworkOrder.getCidr())).thenReturn(false);
//
//        FederatedNetworkOrderController spiedController = Mockito.spy(new FederatedNetworkOrderController());
//        Mockito.doReturn(federatedNetworkOrder).when(spiedController)
//                .getFederatedNetwork(Mockito.eq(federatedNetworkOrder.getId()), Mockito.any(SystemUser.class));
//
//        // exercise
//        spiedController.deleteFederatedNetwork(federatedNetworkOrder.getId(), systemUser);
//
//        // verify
//        PowerMockito.verifyStatic(AgentCommunicatorUtil.class, Mockito.times(1));
//
//        assertEquals(OrderState.FULFILLED, federatedNetworkOrder.getOrderState());
//    }

    //test case: This test check if an error communicating with agent will throw an AgentCommucationException
//    @Test
//    public void testErrorInAgentCommunication() throws FederatedNetworkNotFoundException,
//            NotEmptyFederatedNetworkException, UnauthorizedRequestException, UnexpectedException {
//        //set up
//        mockSingletons();
//        FederatedNetworkOrder federatedNetwork = mock(FederatedNetworkOrder.class);
//        federatedNetwork.setId(FEDERATED_NETWORK_ID);
//        when(federatedNetwork.getSystemUser()).thenReturn(systemUser);
//        when(federatedNetwork.getOrderState()).thenReturn(OrderState.FULFILLED);
//        when(federatedNetworkOrdersHolder.getOrder(FEDERATED_NETWORK_ID)).thenReturn(federatedNetwork);
//
//        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
//        BDDMockito.given(AgentCommunicatorUtil.deleteFederatedNetwork(anyString())).willReturn(false);
//        try {
//            //exercise
//            federatedNetworkOrderController.deleteFederatedNetwork(FEDERATED_NETWORK_ID, systemUser);
//            fail();
//        } catch (AgentCommucationException e) {
//            //verify
//        }
//    }
}
