package cloud.fogbow.fns.core.drivers;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.serviceconnector.AgentConfiguration;
import cloud.fogbow.ras.core.models.UserData;

public interface ServiceDriver {
    void processOpen(FederatedNetworkOrder order) throws FogbowException;

    void processSpawning(FederatedNetworkOrder order) throws FogbowException;

    void processClosed(FederatedNetworkOrder order) throws FogbowException;

    AgentConfiguration configureAgent() throws FogbowException;

    UserData getComputeUserData(AgentConfiguration agentConfiguration, FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException;

    void cleanup(FederatedNetworkOrder order, String hostIp) throws FogbowException;
}
