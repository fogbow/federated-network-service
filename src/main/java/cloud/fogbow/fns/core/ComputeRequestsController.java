package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.ras.api.http.response.ComputeInstance;

public class ComputeRequestsController {

    public void addIpToComputeAllocation(AssignedIp assignedIp, String federatedNetworkId) throws UnexpectedException {
        FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                getFederatedNetworkOrder(federatedNetworkId);
        if (federatedNetworkOrder == null) {
            throw new UnexpectedException();
        }
        federatedNetworkOrder.addAssociatedIp(assignedIp);
        ComputeIdToFederatedNetworkIdMapping.getInstance().put(assignedIp.getComputeId(), federatedNetworkOrder.getId());
    }

    public void addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance, String computeId) {
        String federatedNetworkId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            String instanceIp = federatedNetworkOrder.getAssociatedIp(computeId);
            if (instanceIp != null && computeInstance.getIpAddresses() != null) {
                computeInstance.getIpAddresses().add(instanceIp);
            }
        }
    }

    public FederatedNetworkOrder getFederatedNetworkOrderAssociatedToCompute(String computeId) {
        String federatedNetworkId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);

        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);

            return federatedNetworkOrder;
        }

        return null;
    }
}
