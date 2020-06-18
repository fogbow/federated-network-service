package cloud.fogbow.fns;

import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.core.FederatedNetworkOrderController;
import cloud.fogbow.fns.core.FederatedNetworkOrdersHolder;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.datastore.DatabaseManager;
import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyKeys;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.*;

import static org.mockito.Mockito.when;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({PropertiesHolder.class, DatabaseManager.class})
public class BaseUnitTest {
    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";
    protected TestUtils testUtils;
    protected FederatedNetworkOrderController federatedNetworkOrderController;
    protected FederatedNetworkOrdersHolder federatedNetworkOrdersHolder;
    protected DatabaseManager database;

    public Properties setProperties() {
        Properties p = new Properties();
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY, "fake-file.pem");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.FEDERATED_NETWORK_AGENT_USER_KEY, "fake-user");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY, "fake-private-ip");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY, "fake-public-ip");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.FEDERATED_NETWORK_PRE_SHARED_KEY_KEY, "fake-psk");
        return p;
    }

    @Before
    public void setup() throws InternalServerErrorException {
        this.testUtils = new TestUtils();
        try {
            PowerMockito.mockStatic(PropertiesHolder.class);
            PowerMockito.doCallRealMethod().when(PropertiesHolder.class, "getInstance");
        } catch(Exception ex) { }
    }

    @After
    public void tearDown() {
        new File(TEST_DATABASE_FILE_PATH).delete();
    }

    protected void mockOnlyDatabase() throws InternalServerErrorException {
        SynchronizedDoublyLinkedList<FederatedNetworkOrder> activeOrdersList = new SynchronizedDoublyLinkedList<>();

        this.database = mockDatabaseManager();
        PowerMockito.mockStatic(DatabaseManager.class);
        BDDMockito.given(DatabaseManager.getInstance()).willReturn(database);
        when(this.database.readActiveOrders(Mockito.any(OrderState.class))).thenReturn(activeOrdersList);

        federatedNetworkOrdersHolder = FederatedNetworkOrdersHolder.getInstance();
        federatedNetworkOrderController = new FederatedNetworkOrderController();
    }

    private DatabaseManager mockDatabaseManager() throws InternalServerErrorException {
        DatabaseManager mockedDatabase = Mockito.mock(DatabaseManager.class);
        for (OrderState state : OrderState.values()) {
            Mockito.when(mockedDatabase.readActiveOrders(state)).thenReturn(new SynchronizedDoublyLinkedList<>());
        }
        return mockedDatabase;
    }
}
