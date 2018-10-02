package org.fogbow.federatednetwork.api.parameters;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import java.util.*;

public class FederatedNetwork implements OrderApiParameter<FederatedNetworkOrder> {
    private String cidr;
    private String name;
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
