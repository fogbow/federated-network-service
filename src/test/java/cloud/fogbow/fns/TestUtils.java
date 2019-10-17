package cloud.fogbow.fns;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.datastore.orderstorage.RecoveryService;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TestUtils {
    public static final String ANY_STRING = "any-string";
    public static final String CIDR = "10.150.0.0/28";
    public final String FAKE_COMPUTE_ID = "fake-compute-id";
    public final String FAKE_IP = "192.168.1.0";
    public final String MEMBER = "member";
    public final String USER_ID = "user-id";
    public final String USER_NAME = "user-name";

    private SystemUser user = new SystemUser(USER_ID, USER_NAME, MEMBER);

    @NotNull
    public FederatedNetworkOrder createFederatedNetwork(String id, OrderState state) {
        HashMap<String, MemberConfigurationState> allowedMembers = new HashMap<>();
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        computesIp.add(new AssignedIp(FAKE_COMPUTE_ID, FAKE_IP));
        FederatedNetworkOrder federatedNetworkOrder = new FederatedNetworkOrder(id, user, MEMBER, MEMBER, CIDR,
                "name", allowedMembers, freedIps, computesIp, state, "vanilla");
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
