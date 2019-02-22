package cloud.fogbow.fns.api.http.response;

import cloud.fogbow.fns.core.model.InstanceState;

import java.util.Map;
import java.util.Set;

public class FederatedNetworkInstance {
    private String instanceId;
    private String name;
    private String requestingMember;
    private String providingMember;
    private String cidr;
    private Set<String> providers;
    private Map<String, String> computeIdsAndIps;
    private InstanceState state;

    public FederatedNetworkInstance(String instanceId, String name, String requestingMember, String providingMember,
                                    String cidr, Set<String> providers, Map<String, String> computeIdsAndIps,
                                    InstanceState state) {
        this.instanceId = instanceId;
        this.name = name;
        this.requestingMember = requestingMember;
        this.providingMember = providingMember;
        this.cidr = cidr;
        this.providers = providers;
        this.computeIdsAndIps = computeIdsAndIps;
        this.state = state;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestingMember() {
        return requestingMember;
    }

    public void setRequestingMember(String requestingMember) {
        this.requestingMember = requestingMember;
    }

    public String getProvidingMember() {
        return providingMember;
    }

    public void setProvidingMember(String providingMember) {
        this.providingMember = providingMember;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public void setProviders(Set<String> providers) {
        this.providers = providers;
    }

    public Map<String, String> getComputeIdsAndIps() {
        return computeIdsAndIps;
    }

    public void setComputeIdsAndIps(Map<String, String> computeIdsAndIps) {
        this.computeIdsAndIps = computeIdsAndIps;
    }

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        this.state = state;
    }
}
