package cloud.fogbow.fns.api.http.response;

import cloud.fogbow.fns.constants.ApiDocumentation;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Embeddable;

@Embeddable
public class AssignedIp {
    @ApiModelProperty(position = 0, example = cloud.fogbow.ras.constants.ApiDocumentation.Model.COMPUTE_ID,
            notes = cloud.fogbow.ras.constants.ApiDocumentation.Model.COMPUTE_ID_NOTE)
    private String computeId;
    @ApiModelProperty(position = 1, example = cloud.fogbow.ras.constants.ApiDocumentation.Model.PROVIDER,
            notes = cloud.fogbow.ras.constants.ApiDocumentation.Model.PROVIDER_NOTE)
    private String providerId;
    @ApiModelProperty(position = 2, example = ApiDocumentation.Model.IP, notes = ApiDocumentation.Model.IP_NOTE)
    private String ip;

    public AssignedIp() { }

    public AssignedIp(String computeId, String providerId, String ip) {
        this.computeId = computeId;
        this.providerId = providerId;
        this.ip = ip;
    }

    public String getComputeId() {
        return computeId;
    }

    public String getProviderId() {
        return this.computeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
