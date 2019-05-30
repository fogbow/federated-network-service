package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.ras.core.models.UserData;

import java.io.IOException;

public interface ServiceConnector {
    int acquireVlanId() throws NoVlanIdsLeftException, FogbowException;

    boolean releaseVlanId(int vlanId) throws FogbowException;

    MemberConfigurationState configure(FederatedNetworkOrder order) throws UnexpectedException;

    boolean remove(FederatedNetworkOrder order) throws UnexpectedException;

    boolean removeAgentToComputeTunnel(String hostIp, int vlanId) throws UnexpectedException;

    UserData getTunnelCreationInitScript(String federatedIp, FederatedCompute compute, FederatedNetworkOrder order) throws UnexpectedException;
}
