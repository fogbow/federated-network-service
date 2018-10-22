package org.fogbow.federatednetwork;

import org.junit.After;

import java.io.File;
import java.util.Properties;

import static org.fogbow.federatednetwork.constants.ConfigurationPropertiesKeys.*;

public class BaseUnitTest {

    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";

    public Properties setProperties() {
        Properties p = new Properties();
        p.setProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH, "fake-file.pem");
        p.setProperty(FEDERATED_NETWORK_AGENT_USER, "fake-user");
        p.setProperty(FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS, "fake-private-ip");
        p.setProperty(FEDERATED_NETWORK_AGENT_ADDRESS, "fake-public-ip");
        p.setProperty(FEDERATED_NETWORK_PRE_SHARED_KEY, "fake-psk");
        p.setProperty(ADD_FEDERATED_NETWORK_SCRIPT_PATH, "fake-script-path");
        p.setProperty(REMOVE_FEDERATED_NETWORK_SCRIPT_PATH, "fake-script-path");
        return p;
    }

    @After
    public void tearDown() {
        new File(TEST_DATABASE_FILE_PATH).delete();
    }
}
