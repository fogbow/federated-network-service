package cloud.fogbow.fns.api.http.response;

import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.fns.core.model.InstanceState;
import io.swagger.annotations.ApiModelProperty;

public class InstanceStatus {
    @ApiModelProperty(position = 0, example = ApiDocumentation.Model.INSTANCE_ID)
    private String instanceId;
    @ApiModelProperty(position = 1, example = ApiDocumentation.Model.INSTANCE_NAME)
    private String instanceName;
    @ApiModelProperty(position = 2, example = cloud.fogbow.ras.constants.ApiDocumentation.Model.PROVIDER,
            notes = cloud.fogbow.ras.constants.ApiDocumentation.Model.PROVIDER_NOTE)
    private String provider;
    @ApiModelProperty(position = 3, example = "READY")
    private InstanceState state;

    public InstanceStatus(String instanceId, String provider, InstanceState state) {
        this.instanceId = instanceId;
        this.provider = provider;
        this.state = state;
    }

    public InstanceStatus(String instanceId, String instanceName, String provider, InstanceState state) {
        this.instanceId = instanceId;
        this.instanceName = instanceName;
        this.provider = provider;
        this.state = state;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getProvider() {
        return provider;
    }

    public InstanceState getState() {
        return state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.instanceId == null) ? 0 : this.instanceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        InstanceStatus other = (InstanceStatus) obj;
        if (this.instanceId == null) {
            if (other.getInstanceId() != null) return false;
        } else if (!this.instanceId.equals(other.getInstanceId())) return false;
        return true;
    }
}
