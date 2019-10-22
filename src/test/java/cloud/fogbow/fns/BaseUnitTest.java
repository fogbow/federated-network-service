package cloud.fogbow.fns;

import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyKeys;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.*;

@Ignore
@RunWith(PowerMockRunner.class)
public class BaseUnitTest {
    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";
    protected TestUtils testUtils;

    public Properties setProperties() {
        Properties p = new Properties();
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY, "fake-file.pem");
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY, "fake-user");
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY, "fake-private-ip");
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY, "fake-public-ip");
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_PRE_SHARED_KEY_KEY, "fake-psk");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY, "fake-script-path");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY, "fake-script-path");
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
