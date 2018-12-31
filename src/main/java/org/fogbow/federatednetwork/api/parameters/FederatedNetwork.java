package org.fogbow.federatednetwork.api.parameters;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

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
        order.setProviders(this.providingMembers);
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
