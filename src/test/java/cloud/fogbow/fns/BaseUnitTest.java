package cloud.fogbow.fns;

import cloud.fogbow.fns.core.constants.ConfigurationConstants;
import org.junit.After;

import java.io.File;
import java.util.Properties;

public class BaseUnitTest {

    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";

    public Properties setProperties() {
        Properties p = new Properties();
        p.setProperty(ConfigurationConstants.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH, "fake-file.pem");
        p.setProperty(ConfigurationConstants.FEDERATED_NETWORK_AGENT_USER, "fake-user");
        p.setProperty(ConfigurationConstants.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS, "fake-private-ip");
        p.setProperty(ConfigurationConstants.FEDERATED_NETWORK_AGENT_ADDRESS, "fake-public-ip");
        p.setProperty(ConfigurationConstants.FEDERATED_NETWORK_PRE_SHARED_KEY, "fake-psk");
        p.setProperty(ConfigurationConstants.ADD_FEDERATED_NETWORK_SCRIPT_PATH, "fake-script-path");
        p.setProperty(ConfigurationConstants.REMOVE_FEDERATED_NETWORK_SCRIPT_PATH, "fake-script-path");
        return p;
    }

    @After
    public void tearDown() {
        new File(TEST_DATABASE_FILE_PATH).delete();
    }
}
