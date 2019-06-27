package cloud.fogbow.fns.api.parameters;

import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;

@ApiModel
public class FederatedNetwork implements OrderApiParameter<FederatedNetworkOrder> {
    @ApiModelProperty(position = 0, example = ApiDocumentation.Model.INSTANCE_ID)
    private String name;
    @ApiModelProperty(position = 1, required = true, example = ApiDocumentation.Model.CIDR)
    private String cidr;
    @ApiModelProperty(position = 2, example = ApiDocumentation.Model.PROVIDERS)
    private Set<String> providers;

    @Override
    public FederatedNetworkOrder getOrder() {
        FederatedNetworkOrder order = new FederatedNetworkOrder();
        order.setCidr(this.cidr);
        order.setName(this.name);
        order.setProviders(this.providers);
        return order;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public String getName() {
        return name;
    }

    public String getCidr() {
        return cidr;
    }
}
