package cloud.fogbow.fns.api.parameters;

import cloud.fogbow.fns.core.model.FederatedNetworkOrder;

public interface OrderApiParameter<T extends FederatedNetworkOrder> {
    T getOrder();
}
