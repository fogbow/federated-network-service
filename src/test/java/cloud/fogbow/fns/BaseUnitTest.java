package cloud.fogbow.fns;

import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyKeys;
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

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({PropertiesHolder.class})
public class BaseUnitTest {
    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";
    protected TestUtils testUtils;
    protected PropertiesHolder propertiesHolderMock;
    protected Properties propertiesMock;

    public Properties setProperties() {
        Properties p = new Properties();
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY, "fake-file.pem");
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY, "fake-user");
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY, "fake-private-ip");
        p.setProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY, "fake-public-ip");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.FEDERATED_NETWORK_PRE_SHARED_KEY_KEY, "fake-psk");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY, "fake-script-path");
        p.setProperty(DriversConfigurationPropertyKeys.Vanilla.REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY, "fake-script-path");
        return p;
    }

    @Before
    public void setup() {
        // mock properties
        this.propertiesMock = Mockito.mock(Properties.class);
        this.propertiesHolderMock = Mockito.mock(PropertiesHolder.class);

        Mockito.when(propertiesHolderMock.getProperties(Mockito.anyString())).thenReturn(propertiesMock);
        PowerMockito.mockStatic(PropertiesHolder.class);
        BDDMockito.given(PropertiesHolder.getInstance()).willReturn(propertiesHolderMock);

        this.testUtils = new TestUtils();
    }

    @After
    public void tearDown() {
        new File(TEST_DATABASE_FILE_PATH).delete();
    }
}
