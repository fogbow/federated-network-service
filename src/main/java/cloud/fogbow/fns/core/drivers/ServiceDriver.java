package cloud.fogbow.fns.core.drivers;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;

public interface ServiceDriver {
    void processOpen(FederatedNetworkOrder order) throws FogbowException;

    void processSpawningOrder(FederatedNetworkOrder order) throws FogbowException;

    void processClosingOrder(FederatedNetworkOrder order) throws FogbowException;

    void setupCompute(FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException;

    void terminateCompute(FederatedNetworkOrder order, String hostIp) throws FogbowException;
}
