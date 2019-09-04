package cloud.fogbow.fns;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.datastore.orderstorage.RecoveryService;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.ras.api.parameters.Compute;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import java.util.*;

public class TestUtils {
    public static final String CIDR = "10.150.0.0/28";
    public final String FAKE_COMPUTE_ID = "fake-compute-id";
    public final String FAKE_IP = "192.168.1.0";
    public final String MEMBER = "member";
    public final String USER_ID = "user-id";
    public final String USER_NAME = "user-name";
    public static final String FAKE_TOKEN = "fake-token";
    public static final int RUN_ONCE = 1;
    public static final String EMPTY_STRING = "";
    public SystemUser user = new SystemUser(USER_ID, USER_NAME, MEMBER);
    public Gson gson = new Gson();

    @NotNull
    public FederatedNetworkOrder createFederatedNetwork(String id, OrderState state) {
        HashMap<String, MemberConfigurationState> allowedMembers = new HashMap<>();
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        computesIp.add(new AssignedIp(FAKE_COMPUTE_ID, FAKE_IP));
        FederatedNetworkOrder federatedNetworkOrder = Mockito.spy(new FederatedNetworkOrder(id, user, MEMBER, MEMBER, CIDR,
                "name", allowedMembers, freedIps, computesIp, state));
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

    public FederatedCompute createFederatedCompute(String fedNetId) {
        Compute compute = new Compute();
        FederatedCompute federatedCompute = new FederatedCompute();
        federatedCompute.setCompute(compute);
        federatedCompute.setFederatedNetworkId(fedNetId);
        return federatedCompute;
    }
}
