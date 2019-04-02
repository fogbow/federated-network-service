package cloud.fogbow.fns.api.parameters;

import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;

import java.util.*;

public class FederatedNetwork implements OrderApiParameter<FederatedNetworkOrder> {
    private String cidr;
    private String name;
    private Set<String> providingMembers;

    @Override
    public FederatedNetworkOrder getOrder() {
        FederatedNetworkOrder order = new FederatedNetworkOrder();
        order.setCidr(this.cidr);
        order.setName(this.name);
        order.setProviders(FederatedNetworkUtil.initializeMemberConfigurationMap(this.providingMembers));

        return order;
    }

    public Set<String> getProvidingMembers() {
        return providingMembers;
    }

    public String getName() {
        return name;
    }

    public String getCidr() {
        return cidr;
    }
}
