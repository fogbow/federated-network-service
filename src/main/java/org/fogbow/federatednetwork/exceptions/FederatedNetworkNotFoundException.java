package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class FederatedNetworkNotFoundException extends FogbowFnsException {
    private static final long serialVersionUID = 1L;

    public FederatedNetworkNotFoundException(String id) {
        super(String.format(Messages.Exception.UNABLE_TO_FIND_FEDERATED_NETWORK, id));
    }

    public FederatedNetworkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
