package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.api.parameters.Compute;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.exceptions.UnexpectedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.utils.FederatedComputeUtil;
import org.fogbow.federatednetwork.utils.PropertiesHolder;

import org.fogbowcloud.ras.core.models.instances.ComputeInstance;

import java.io.IOException;
import java.sql.SQLException;

import static org.fogbow.federatednetwork.constants.ConfigurationConstants.FEDERATED_NETWORK_AGENT_ADDRESS;
import static org.fogbow.federatednetwork.constants.ConfigurationConstants.FEDERATED_NETWORK_PRE_SHARED_KEY;

public class ComputeRequestsController {

    // Compute methods

    public String addScriptToSetupTunnelIfNeeded(Compute compute, String federatedNetworkId)
                                throws FederatedNetworkNotFoundException, SQLException, InvalidCidrException,
                                        UnexpectedException, SubnetAddressesCapacityReachedException, IOException {
        String instanceIp = null;
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            if (federatedNetworkOrder == null) {
                throw new FederatedNetworkNotFoundException(federatedNetworkId);
            }
            String cidr = federatedNetworkOrder.getCidr();
            instanceIp = federatedNetworkOrder.getFreeIp();
            FederatedComputeUtil.addUserData(compute, instanceIp,
                    PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_ADDRESS), cidr,
                    PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_PRE_SHARED_KEY));
        }
        return instanceIp;
    }

    public void addIpToComputeAllocation(String instanceIp, String computeId, String federatedNetworkId)
            throws SQLException, UnexpectedException {
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            if (federatedNetworkOrder == null) {
                throw new UnexpectedException();
            }
            federatedNetworkOrder.addAssociatedIp(computeId, instanceIp);
        }
    }

    public void removeIpToComputeAllocation(String computeId) throws SQLException {
        String federatedNetworkId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            federatedNetworkOrder.removeAssociatedIp(computeId);
        }
    }

    public void addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance, String computeId) throws SQLException {
        String federatedNetworkId = ComputeIdToFederatedNetworkIdMapping.getInstance().get(computeId);
        if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
            FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().
                    getFederatedNetworkOrder(federatedNetworkId);
            String instanceIp = federatedNetworkOrder.getAssociatedIp(computeId);
            computeInstance.getIpAddresses().add(instanceIp);
        }
    }
}
