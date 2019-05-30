package cloud.fogbow.fns.api.http.response;

import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.fns.core.model.InstanceState;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Set;

public class FederatedNetworkInstance {
    @ApiModelProperty(position = 0, example = ApiDocumentation.Model.INSTANCE_ID)
    private String instanceId;
    @ApiModelProperty(position = 1, example = ApiDocumentation.Model.INSTANCE_NAME)
    private String name;
    @ApiModelProperty(position = 2, example = ApiDocumentation.Model.REQUESTER)
    private String requester;
    @ApiModelProperty(position = 3, example = ApiDocumentation.Model.PROVIDER)
    private String provider;
    @ApiModelProperty(position = 4, example = ApiDocumentation.Model.CIDR)
    private String cidr;
    @ApiModelProperty(position = 5, example = ApiDocumentation.Model.PROVIDERS)
    private Set<String> providers;
    @ApiModelProperty(position = 6, example = ApiDocumentation.Model.COMPUTE_LIST)
    private List<AssignedIp> assignedIps;
    @ApiModelProperty(position = 7)
    private InstanceState state;

    public FederatedNetworkInstance(String instanceId, String name, String requester, String provider,
                                    String cidr, Set<String> providers, List<AssignedIp> assignedIps,
                                    InstanceState state) {
        this.instanceId = instanceId;
        this.name = name;
        this.requester = requester;
        this.provider = provider;
        this.cidr = cidr;
        this.providers = providers;
        this.assignedIps = assignedIps;
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

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public List<AssignedIp> getAssignedIps() {
        return assignedIps;
    }

    public void setAssignedIps(List<AssignedIp> assignedIps) {
        this.assignedIps = assignedIps;
    }

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        this.state = state;
    }
}
