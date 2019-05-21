package cloud.fogbow.fns.api.http.response;

import cloud.fogbow.fns.constants.ApiDocumentation;
import io.swagger.annotations.ApiModelProperty;

public class AssignedIp {
    @ApiModelProperty(position = 0, example = cloud.fogbow.ras.constants.ApiDocumentation.Model.COMPUTE_ID,
            notes = cloud.fogbow.ras.constants.ApiDocumentation.Model.COMPUTE_ID_NOTE)
    private String computeId;
    @ApiModelProperty(position = 1, example = ApiDocumentation.Model.IP, notes = ApiDocumentation.Model.IP_NOTE)
    private String ip;

    public AssignedIp(String computeId, String ip) {
        this.computeId = computeId;
        this.ip = ip;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
