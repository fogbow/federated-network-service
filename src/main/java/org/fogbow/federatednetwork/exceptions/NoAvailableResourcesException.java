package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class NoAvailableResourcesException extends FogbowFnsException {
    private static final long serialVersionUID = 1L;

    public NoAvailableResourcesException() {
        super(Messages.Exception.NO_AVAILABLE_RESOURCES);
    }

    public NoAvailableResourcesException(String message) {
        super(message);
    }

    public NoAvailableResourcesException(String message, Throwable cause) {
        super(message, cause);
    }
}
