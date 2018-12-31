package org.fogbow.federatednetwork.api.parameters;

import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

public interface OrderApiParameter<T extends FederatedNetworkOrder> {
    T getOrder();
}
