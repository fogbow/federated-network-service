package org.fogbow.federatednetwork.model;

import org.fogbowcloud.ras.core.models.instances.ComputeInstance;
import org.fogbowcloud.ras.core.models.instances.InstanceState;

import java.util.List;

public class FederatedComputeInstance extends ComputeInstance {

    private String federatedIp;

    public FederatedComputeInstance(String id, String hostName, int vCPU, int memory, InstanceState state,
                                    List<String> ipAddresses, int disk, String federatedIp) {

        super(id, state, hostName, vCPU, memory, disk, ipAddresses);
        this.federatedIp = federatedIp;
    }

    public FederatedComputeInstance(ComputeInstance computeInstance, String federatedIp) {
        this(computeInstance.getId(), computeInstance.getHostName(), computeInstance.getvCPU(),
                computeInstance.getRam(), computeInstance.getState(), computeInstance.getIpAddresses(),
                computeInstance.getDisk(), federatedIp);
    }

    public FederatedComputeInstance(String id) {
        super(id);
    }

    public String getFederatedIp() {
        return federatedIp;
    }

    public void setFederatedIp(String federatedIp) {
        this.federatedIp = federatedIp;
    }
}
