package org.fogbow.federatednetwork.model;

import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.util.List;
import java.util.UUID;

public class FederatedComputeOrderOld extends ComputeOrder {

	private String federatedNetworkId;

	public FederatedComputeOrderOld() {
		super();
	}

	public FederatedComputeOrderOld(FederationUser federationUser, String requestingMember, String providingMember,
									int vCPU, int memory, int disk, String imageId, UserData userData, String publicKey,
									List<String> networksId, String federatedNetworkId) {
		super(federationUser, requestingMember, providingMember, vCPU, memory, disk, imageId, userData, publicKey, networksId);
		this.federatedNetworkId = federatedNetworkId;
	}

	public FederatedComputeOrderOld(ComputeOrder computeOrder, String federatedNetworkId) {
		super(computeOrder.getId(), computeOrder.getFederationUser(), computeOrder.getRequestingMember(),
				computeOrder.getProvidingMember(), computeOrder.getvCPU(), computeOrder.getMemory(),
				computeOrder.getDisk(), computeOrder.getImageId(), computeOrder.getUserData(),
				computeOrder.getPublicKey(), computeOrder.getNetworksId());
		this.federatedNetworkId = federatedNetworkId;
	}

	public String getFederatedNetworkId() {
		return federatedNetworkId;
	}

	public void setFederatedNetworkId(String federatedNetworkId) {
		this.federatedNetworkId = federatedNetworkId;
	}
}
