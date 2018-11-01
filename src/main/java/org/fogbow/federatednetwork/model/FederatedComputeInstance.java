package org.fogbow.federatednetwork.model;

import org.fogbowcloud.ras.core.models.instances.ComputeInstance;
import org.fogbowcloud.ras.core.models.instances.InstanceState;
import org.fogbowcloud.ras.core.models.orders.UserData;

import java.util.List;

public class FederatedComputeInstance extends ComputeInstance {

    private String federatedIp;

    public FederatedComputeInstance(String id, String hostName, int vCPU, int memory, InstanceState state,
                                    int disk, List<String> ipAddresses, String image, String publicKey,
                                    List<UserData> userData, String federatedIp) {

        super(id, state, hostName, vCPU, memory, disk, ipAddresses, image, publicKey, userData);
        this.federatedIp = federatedIp;
    }

    public FederatedComputeInstance(ComputeInstance computeInstance, String federatedIp) {
        this(computeInstance.getId(), computeInstance.getName(), computeInstance.getvCPU(),
             computeInstance.getMemory(), computeInstance.getState(), computeInstance.getDisk(),
             computeInstance.getIpAddresses(), computeInstance.getImageId(), computeInstance.getPublicKey(),
             computeInstance.getUserData(), federatedIp);
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
