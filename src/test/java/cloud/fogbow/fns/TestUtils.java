package cloud.fogbow.fns;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.datastore.orderstorage.RecoveryService;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.OrderState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TestUtils {
    public static final String ANY_STRING = "any-string";
    public static final String CIDR = "10.150.0.0/28";
    public static final String FAKE_INSTANCE_ID = "intance-id";
    public static final String FAKE_PROVIDER_ID = "fake-local-member";
    public static final String FAKE_HOST_IP = "10.0.0.1";
    public static final String FAKE_PROVIDER = "fake-provider";
    public static final String FAKE_PUBLIC_KEY = "fake-public-key";
    public static final String FAKE_VLAN_ID_SERVICE_URL = "http://fake.vlan.service";
    public static final int FAKE_VLAN_ID = 42;
    public final String FAKE_COMPUTE_ID = "fake-compute-id";
    public final String FAKE_IP = "192.168.1.0";
    public final String MEMBER = "member";
    public final String USER_ID = "user-id";
    public final String USER_NAME = "user-name";

    public SystemUser user = new SystemUser(USER_ID, USER_NAME, MEMBER);
    public static final String FAKE_USER_TOKEN = "fake-user-token";
    public static final String FAKE_ID = "Fake-id";
    public static final int RUN_ONCE = 1;

    @NotNull
    public FederatedNetworkOrder createFederatedNetwork(String id, OrderState state) {
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        computesIp.add(new AssignedIp(FAKE_COMPUTE_ID, FAKE_PROVIDER_ID, FAKE_IP));
        FederatedNetworkOrder federatedNetworkOrder = new FederatedNetworkOrder(id, user, MEMBER, MEMBER, CIDR,
                "name", freedIps, computesIp, state, "vanilla");
        return federatedNetworkOrder;
    }

    public List<FederatedNetworkOrder> populateFedNetDbWithState(OrderState state, int size, RecoveryService service) throws UnexpectedException {
        List<FederatedNetworkOrder> orders = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            FederatedNetworkOrder order = createFederatedNetwork(String.valueOf(UUID.randomUUID()), state);
            orders.add(order);
            service.put(order);
        }
        return orders;
    }

    public SystemUser createSystemUser() {
        return user;
    }
}
