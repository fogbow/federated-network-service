package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class NotEmptyFederatedNetworkException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotEmptyFederatedNetworkException() {
		super(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK);
	}
}
