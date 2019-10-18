package cloud.fogbow.fns.api.parameters;

import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.ServiceListController;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.fns.constants.ApiDocumentation;
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
    @ApiModelProperty(position = 3, example = ApiDocumentation.Model.DFNS_EXAMPLE)
    private String serviceName;

    @Override
    public FederatedNetworkOrder getOrder() {
        FederatedNetworkOrder order = new FederatedNetworkOrder();
        order.setCidr(this.cidr);
        order.setRequester(PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.PROVIDER_ID_KEY));
        order.setProvider(order.getRequester());
        order.setName(this.name == null ? SystemConstants.FOGBOW_INSTANCE_NAME_PREFIX+UUID.randomUUID() : this.name);
        order.setProviders(FederatedNetworkUtil.initializeMemberConfigurationMap(this.providers));
        order.setServiceName(this.serviceName == null ? new ServiceListController().getDefaultService() : this.serviceName);
        return order;
    }

    public String getCidr() {
        return cidr;
    }

    public String getName() {
        return name;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
