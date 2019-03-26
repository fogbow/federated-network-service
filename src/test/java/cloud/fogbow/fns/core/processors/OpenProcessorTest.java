package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.HashMap;


public class OpenProcessorTest {
    // TODO ARNETT Adapt these tests
//    @Test
//    public void testFailureWhileActivatingFederatedNetwork() throws InvalidCidrException, UnexpectedException {
//        // set up
//        mockDatabase(new HashMap<>());
//
//        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
//        Mockito.when(AgentCommunicatorUtil.createFederatedNetwork(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
//
//        // exercise
//        new FederatedNetworkOrderController().addFederatedNetwork(federatedNetworkOrder, systemUser);
//
//        // verify
//        PowerMockito.verifyStatic(AgentCommunicatorUtil.class, Mockito.times(1));
//
//        assertEquals(OrderState.FAILED, federatedNetworkOrder.getOrderState());
//    }
//
//    @Test
//    public void testSuccessWhileActivatingFederatedNetwork() throws InvalidCidrException, UnexpectedException {
//        // set up
//        mockDatabase(new HashMap<>());
//
//        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
//        Mockito.when(AgentCommunicatorUtil.createFederatedNetwork(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//
//        // exercise
//        new FederatedNetworkOrderController().addFederatedNetwork(federatedNetworkOrder, systemUser);
//
//        // verify
//        PowerMockito.verifyStatic(AgentCommunicatorUtil.class, Mockito.times(1));
//
//        assertEquals(OrderState.FULFILLED, federatedNetworkOrder.getOrderState());
//    }

}
