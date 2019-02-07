package cloud.fogbow.fns.core.exceptions;

import cloud.fogbow.fns.constants.Messages;

public class NotEmptyFederatedNetworkException extends Exception {
	private static final long serialVersionUID = 1L;

	public NotEmptyFederatedNetworkException() {
		super(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK);
	}

	public NotEmptyFederatedNetworkException(String message) {
		super(message);
	}

	public NotEmptyFederatedNetworkException(String message, Throwable cause) {
		super(message, cause);
	}
}
