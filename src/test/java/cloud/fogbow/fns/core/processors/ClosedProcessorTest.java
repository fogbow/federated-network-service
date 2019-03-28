package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.MockedFederatedNetworkUnitTests;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ClosedProcessorTest extends MockedFederatedNetworkUnitTests {
    public static final long SLEEP_TIME = 1000L;

    @Test
    public void testSuccessWhileDeletingFederatedNetwork() throws UnexpectedException {
        // set up
        mockOnlyDatabase();
        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        Mockito.when(AgentCommunicatorUtil.deleteFederatedNetwork(Mockito.anyString())).thenReturn(true);

        SystemUser systemUser = new SystemUser("userId", "userName", "identityProviderId");
        FederatedNetworkOrder order = new FederatedNetworkOrder("id", systemUser, "requestingMember",
                "providingMember", "10.0.30.1/20", "name", new HashSet<>(), new LinkedList<>(), new HashMap<>(), OrderState.CLOSED);

        FederatedNetworkOrdersHolder.getInstance().insertNewOrder(order);

        // exercise
        ClosedProcessor closedProcessor = new ClosedProcessor(new FederatedNetworkOrderController(), SLEEP_TIME);
        closedProcessor.processOrder(order);

        // verify
        Assert.assertEquals(OrderState.DEACTIVATED, order.getOrderState());
    }

    @Test(expected = UnexpectedException.class)
    public void testFailureWhileDeletingFederatedNetwork() throws UnexpectedException {
        // set up
        mockOnlyDatabase();
        PowerMockito.mockStatic(AgentCommunicatorUtil.class);
        Mockito.when(AgentCommunicatorUtil.deleteFederatedNetwork(Mockito.anyString())).thenReturn(false);

        SystemUser systemUser = new SystemUser("userId", "userName", "identityProviderId");
        FederatedNetworkOrder order = new FederatedNetworkOrder("id", systemUser, "requestingMember",
                "providingMember", "10.0.30.1/20", "name", new HashSet<>(), new LinkedList<>(), new HashMap<>(), OrderState.CLOSED);

        FederatedNetworkOrdersHolder.getInstance().insertNewOrder(order);

        // exercise
        ClosedProcessor closedProcessor = new ClosedProcessor(new FederatedNetworkOrderController(), SLEEP_TIME);
        closedProcessor.processOrder(order);
    }
}
