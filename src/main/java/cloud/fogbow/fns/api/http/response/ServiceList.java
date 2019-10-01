package cloud.fogbow.fns.api.http.response;

import cloud.fogbow.fns.constants.ApiDocumentation;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class ServiceList {
    @ApiModelProperty(position = 0, example = ApiDocumentation.Model.SERVICE_LIST)
    private List<String> services;

    private ServiceList() {}

    public ServiceList(List<String> services) {
        this.services = services;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> clouds) {
        this.services = clouds;
    }
}
