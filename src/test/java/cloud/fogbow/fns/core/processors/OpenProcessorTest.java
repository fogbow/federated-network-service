package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.MockedFederatedNetworkUnitTests;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.OrderStateTransitioner;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.*;


public class OpenProcessorTest extends MockedFederatedNetworkUnitTests {
    @Test
    public void testFailureWhileActivatingFederatedNetwork() throws InvalidCidrException, UnexpectedException {
        // set up
        FederatedNetworkOrderController orderController = new FederatedNetworkOrderController();
        mockOnlyDatabase();
        SystemUser systemUser = new SystemUser("userId", "userName", "identityProviderId");
        FederatedNetworkOrder order = new FederatedNetworkOrder("id", systemUser, "requestingMember",
                "providingMember", "10.0.30.1/20", "name", new HashSet<>(), new LinkedList<>(), new HashMap<>(), null);

        orderController.activateOrder(order);

        OpenProcessor openProcessor = new OpenProcessor(1000L);
        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        Mockito.when(AgentCommunicatorUtil.createFederatedNetwork(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        // exercise
        openProcessor.processOrder(order);

        // verify
        PowerMockito.verifyStatic(AgentCommunicatorUtil.class, Mockito.times(1));

        Assert.assertEquals(OrderState.FAILED, order.getOrderState());
    }

    @Test
    public void testSuccessWhileActivatingFederatedNetwork() throws InvalidCidrException, UnexpectedException {
        // set up
        FederatedNetworkOrderController orderController = new FederatedNetworkOrderController();
        mockOnlyDatabase();
        SystemUser systemUser = new SystemUser("userId", "userName", "identityProviderId");
        FederatedNetworkOrder order = new FederatedNetworkOrder("id", systemUser, "requestingMember",
                "providingMember", "10.0.30.1/20", "name", new HashSet<>(), new LinkedList<>(), new HashMap<>(), null);

        orderController.activateOrder(order);

        OpenProcessor openProcessor = new OpenProcessor(1000L);
        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        Mockito.when(AgentCommunicatorUtil.createFederatedNetwork(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        // exercise
        openProcessor.processOrder(order);

        // verify
        PowerMockito.verifyStatic(AgentCommunicatorUtil.class, Mockito.times(1));

        Assert.assertEquals(OrderState.FULFILLED, order.getOrderState());
    }
}
