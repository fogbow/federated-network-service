package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.drivers.ServiceDriver;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;

public class DfnsServiceDriver implements ServiceDriver {
    @Override
    public void processOpen(FederatedNetworkOrder order) throws FogbowException {

    }

    @Override
    public void processSpawningOrder(FederatedNetworkOrder order) throws FogbowException {

    }

    @Override
    public void processClosingOrder(FederatedNetworkOrder order) throws FogbowException {

    }

    @Override
    public void setupCompute(FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException {

    }

    @Override
    public void terminateCompute(FederatedNetworkOrder order, String hostIp) throws FogbowException {

    }
}
