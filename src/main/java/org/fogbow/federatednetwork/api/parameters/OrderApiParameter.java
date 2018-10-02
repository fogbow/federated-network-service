package org.fogbow.federatednetwork.api.parameters;

import org.fogbow.federatednetwork.model.FederatedOrder;

public interface OrderApiParameter<T extends FederatedOrder> {
    T getOrder();
}
