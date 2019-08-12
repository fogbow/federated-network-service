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

@Ignore
@PowerMockIgnore({"javax.management.*"})
@PrepareForTest({ComputeIdToFederatedNetworkIdMapping.class, OrderRepository.class})
@PowerMockRunnerDelegate(SpringRunner.class)
@RunWith(PowerMockRunner.class)
@SpringBootTest
public class BaseUnitTest {
    public static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";
    protected static final String CIDR = "10.150.0.0/28";
    protected final String FAKE_COMPUTE_ID = "fake-compute-id";
    protected final String FAKE_IP = "192.168.1.0";
    protected final String MEMBER = "member";
    protected final String USER_ID = "user-id";
    protected final String USER_NAME = "user-name";
    protected SystemUser user = new SystemUser(USER_ID, USER_NAME, MEMBER);

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

    @NotNull
    protected FederatedNetworkOrder createFederatedNetwork(String id, OrderState state) {
        HashMap<String, MemberConfigurationState> allowedMembers = new HashMap<>();
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        computesIp.add(new AssignedIp(FAKE_COMPUTE_ID, FAKE_IP));
        FederatedNetworkOrder federatedNetworkOrder = new FederatedNetworkOrder(id, user, MEMBER, MEMBER, CIDR,
                "name", allowedMembers, freedIps, computesIp, state);
        return federatedNetworkOrder;
    }

    @After
    public void tearDown() {
        new File(TEST_DATABASE_FILE_PATH).delete();
    }
}
