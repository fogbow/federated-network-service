package cloud.fogbow.fns;

import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.datastore.DatabaseManager;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedComputeUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/*
This class should be ignored because it doesn't test
 */
@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({AgentCommunicatorUtil.class, FederatedNetworkUtil.class, FederatedComputeUtil.class,
        DatabaseManager.class, FederatedNetworkOrdersHolder.class})
public class MockedFederatedNetworkUnitTests extends BaseUnitTest {
    protected FederatedNetworkOrderController federatedNetworkOrderController;
    protected FederatedNetworkOrdersHolder federatedNetworkOrdersHolder;
    protected DatabaseManager database;

    protected void mockSingletons() {
        Map<String, FederatedNetworkOrder> activeOrdersMap = new HashMap<>();
        mockDatabase(activeOrdersMap);
        mockSharedOrderHolders();
        try {
            federatedNetworkOrderController = Mockito.spy(new FederatedNetworkOrderController());
        } catch (Exception e) {
            fail();
        }
    }

    protected void mockSharedOrderHolders() {
        federatedNetworkOrdersHolder = Mockito.mock(FederatedNetworkOrdersHolder.class);
        PowerMockito.mockStatic(FederatedNetworkOrdersHolder.class);
        try {
            BDDMockito.given(FederatedNetworkOrdersHolder.getInstance()).willReturn(federatedNetworkOrdersHolder);
        } catch (Exception e) {
            fail();
        }
    }

    protected void mockDatabase(Map<String, FederatedNetworkOrder> activeOrdersMap) {
        database = Mockito.mock(DatabaseManager.class);
        PowerMockito.mockStatic(DatabaseManager.class);
        BDDMockito.given(DatabaseManager.getInstance()).willReturn(database);
        try {
//            when(database.retrieveActiveFederatedOrders()).thenReturn(activeOrdersMap);
        } catch (Exception e) {
            fail();
        }
    }

    protected void mockOnlyDatabase() {
        Map<String, FederatedNetworkOrder> activeOrdersMap = new HashMap<>();

        this.database = mockDatabaseManager();
        PowerMockito.mockStatic(DatabaseManager.class);
        BDDMockito.given(DatabaseManager.getInstance()).willReturn(database);
//        when(this.database.retrieveActiveFederatedOrders()).thenReturn(activeOrdersMap);

        federatedNetworkOrdersHolder = FederatedNetworkOrdersHolder.getInstance();
        federatedNetworkOrderController = new FederatedNetworkOrderController();
    }

    private DatabaseManager mockDatabaseManager() {
        DatabaseManager mockedDatabase = Mockito.mock(DatabaseManager.class);
        for (OrderState state : OrderState.values()) {
            Mockito.when(mockedDatabase.readActiveOrders(state)).thenReturn(new SynchronizedDoublyLinkedList<>());
        }
        return mockedDatabase;
    }
}
