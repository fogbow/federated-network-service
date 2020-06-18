package cloud.fogbow.fns.core.processors;

import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.fns.BaseUnitTest;
import cloud.fogbow.fns.TestUtils;
import cloud.fogbow.fns.core.OrderStateTransitioner;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest({OrderStateTransitioner.class})
public class OpenProcessorTest extends BaseUnitTest {

    private OpenProcessor openProcessor;

    @Before
    public void setup() throws InternalServerErrorException {
        super.setup();
        mockOnlyDatabase();
        this.openProcessor = Mockito.spy(new OpenProcessor(0L));
        PowerMockito.mockStatic(OrderStateTransitioner.class);
    }

    //test case: Checking if when the transition doesn't work the failed transition is done
    @Test
    public void testFailureWhileActivatingFederatedNetwork() throws Exception {
        //setup
        PowerMockito.mockStatic(OrderStateTransitioner.class);
        PowerMockito.doThrow(new InternalServerErrorException()).when(OrderStateTransitioner.class, "transition", Mockito.any(), Mockito.eq(OrderState.SPAWNING));
        PowerMockito.doNothing().when(OrderStateTransitioner.class, "transition", Mockito.any(), Mockito.eq(OrderState.FAILED));
        FederatedNetworkOrder federatedNetworkOrder = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        try {
            //exercise
            openProcessor.processOrder(federatedNetworkOrder);
        } catch (InternalServerErrorException ex) {
            //verify
            Assert.assertEquals(federatedNetworkOrder.getVlanId(), -1);
            PowerMockito.verifyStatic(OrderStateTransitioner.class, Mockito.times(TestUtils.RUN_ONCE));
            OrderStateTransitioner.transition(federatedNetworkOrder, OrderState.FAILED);
        }

    }

    //test case: Check if the transition is called
    @Test
    public void testSuccessWhileActivatingFederatedNetwork() throws Exception {
        //setup
        PowerMockito.mockStatic(OrderStateTransitioner.class);
        PowerMockito.doNothing().when(OrderStateTransitioner.class, "transition", Mockito.any(), Mockito.any());
        FederatedNetworkOrder federatedNetworkOrder = testUtils.createFederatedNetwork(TestUtils.FAKE_ID, OrderState.OPEN);
        //exercise
        openProcessor.processOrder(federatedNetworkOrder);
        //verify
        Assert.assertEquals(federatedNetworkOrder.getVlanId(), -1);
        PowerMockito.verifyStatic(OrderStateTransitioner.class, Mockito.times(TestUtils.RUN_ONCE));
        OrderStateTransitioner.transition(federatedNetworkOrder, OrderState.SPAWNING);
    }
}
