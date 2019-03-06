package cloud.fogbow.fns;

import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import org.junit.After;

import java.io.File;
import java.util.Properties;

public class BaseUnitTest {
    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";

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

    @After
    public void tearDown() {
        new File(TEST_DATABASE_FILE_PATH).delete();
    }
}
