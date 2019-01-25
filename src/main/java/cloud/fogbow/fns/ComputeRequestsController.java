package cloud.fogbow.fns;

import cloud.fogbow.fns.api.parameters.Compute;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationConstants;
import cloud.fogbow.fns.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.exceptions.InvalidCidrException;
import cloud.fogbow.fns.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.model.FederatedNetworkOrder;
import cloud.fogbow.fns.utils.FederatedComputeUtil;

import org.fogbowcloud.ras.core.models.instances.ComputeInstance;

import java.io.IOException;

public class ComputeRequestsController {

    // Compute methods
    public String addScriptToSetupTunnelIfNeeded(Compute compute, String federatedNetworkId)
            throws FederatedNetworkNotFoundException, InvalidCidrException,
            SubnetAddressesCapacityReachedException, IOException, UnexpectedException {
        String instanceIp = null;
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            if (federatedNetworkOrder == null) {
                throw new FederatedNetworkNotFoundException(federatedNetworkId);
            }
            instanceIp = federatedNetworkOrder.getFreeIp();
            String cidr = federatedNetworkOrder.getCidr();
            FederatedComputeUtil.addUserData(compute, instanceIp,
                    PropertiesHolder.getInstance().getProperty(ConfigurationConstants.FEDERATED_NETWORK_AGENT_ADDRESS), cidr,
                    PropertiesHolder.getInstance().getProperty(ConfigurationConstants.FEDERATED_NETWORK_PRE_SHARED_KEY));
        }
        return instanceIp;
    }

    public void addIpToComputeAllocation(String instanceIp, String computeId, String federatedNetworkId)
            throws UnexpectedException {
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            if (federatedNetworkOrder == null) {
                throw new UnexpectedException();
            }
            federatedNetworkOrder.addAssociatedIp(computeId, instanceIp);
        }
    }

    public void removeIpToComputeAllocation(String computeId) {
        String federatedNetworkId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            federatedNetworkOrder.removeAssociatedIp(computeId);
        }
    }

    public void addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance, String computeId) {
        String federatedNetworkId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            String instanceIp = federatedNetworkOrder.getAssociatedIp(computeId);
            if (instanceIp != null) {
                computeInstance.getIpAddresses().add(instanceIp);
            }
        }
    }
}
