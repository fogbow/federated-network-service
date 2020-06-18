package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.ras.api.http.response.ComputeInstance;

public class ComputeRequestsController {

    public void addIpToComputeAllocation(AssignedIp assignedIp, String federatedNetworkId) throws InternalServerErrorException {
        FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(federatedNetworkId);
        if (federatedNetworkOrder == null) {
            throw new InternalServerErrorException();
        }
        federatedNetworkOrder.addAssociatedIp(assignedIp);
        ComputeIdToFederatedNetworkIdMapping.getInstance().put(assignedIp.getComputeId(), federatedNetworkOrder.getId());
    }

    public void addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance, String computeId) throws InternalServerErrorException {
        String federatedNetworkId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(federatedNetworkId);
            String instanceIp = federatedNetworkOrder.getAssociatedIp(computeId);
            if (instanceIp != null && computeInstance.getIpAddresses() != null) {
                computeInstance.getIpAddresses().add(instanceIp);
            }
        }
    }

    public FederatedNetworkOrder getFederatedNetworkOrderAssociatedToCompute(String computeId) throws InternalServerErrorException {
        String fedNetId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);

        if (fedNetId != null && !fedNetId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(fedNetId);

            return federatedNetworkOrder;
        }

        return null;
    }
}
