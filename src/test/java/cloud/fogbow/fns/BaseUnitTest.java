package cloud.fogbow.fns;

import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.ComputeIdToFederatedNetworkIdMapping;
import cloud.fogbow.fns.core.datastore.orderstorage.OrderRepository;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.*;

public class BaseUnitTest {
    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";
    protected TestUtils testUtils;

    public Properties setProperties() {
        Properties p = new Properties();
        p.setProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY, "fake-file.pem");
        p.setProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY, "fake-user");
        p.setProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY, "fake-private-ip");
        p.setProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY, "fake-public-ip");
        p.setProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_PRE_SHARED_KEY_KEY, "fake-psk");
        p.setProperty(ConfigurationPropertyKeys.ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY, "fake-script-path");
        p.setProperty(ConfigurationPropertyKeys.REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY, "fake-script-path");
        return p;
    }

    @Before
    public void setup() {
        this.testUtils = new TestUtils();
    }

    @After
    public void tearDown() {
        new File(TEST_DATABASE_FILE_PATH).delete();
    }
}
