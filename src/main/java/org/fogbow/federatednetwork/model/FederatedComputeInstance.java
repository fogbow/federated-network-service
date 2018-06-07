package org.fogbow.federatednetwork.model;

import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.instances.InstanceState;

public class FederatedComputeInstance extends ComputeInstance {

	private String federatedIp;

	public FederatedComputeInstance(String id, String hostName, int vCPU, int memory, InstanceState state,
	                                String localIpAddress, String sshPublicAddress, String sshUserName,
	                                String sshExtraPorts, String federatedIp) {

		super(id, hostName, vCPU, memory, state, localIpAddress, sshPublicAddress, sshUserName, sshExtraPorts);
		this.federatedIp = federatedIp;
	}

	public FederatedComputeInstance(ComputeInstance computeInstance, String federatedIp) {
		this(computeInstance.getId(), computeInstance.getHostName(), computeInstance.getvCPU(),
				computeInstance.getMemory(), computeInstance.getState(), computeInstance.getLocalIpAddress(),
				null, null, null, federatedIp);
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
