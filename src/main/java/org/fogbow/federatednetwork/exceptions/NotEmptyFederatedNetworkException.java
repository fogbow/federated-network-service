package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.FederatedNetworkConstants;

public class NotEmptyFederatedNetworkException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotEmptyFederatedNetworkException() {
		super(FederatedNetworkConstants.CANNOT_REMOVE_FEDERATED_NETWORK_HAS_COMPUTE_ASSOCIATED);
	}
}
