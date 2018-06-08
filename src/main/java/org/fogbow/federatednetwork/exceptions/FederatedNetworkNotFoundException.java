package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.FederatedNetworkConstants;

public class FederatedNetworkNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public FederatedNetworkNotFoundException() {
		super(FederatedNetworkConstants.FEDERATED_NETWORK_NOT_FOUND);
	}

}
