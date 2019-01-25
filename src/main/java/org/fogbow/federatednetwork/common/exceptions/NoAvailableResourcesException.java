package org.fogbow.federatednetwork.common.exceptions;

import org.fogbow.federatednetwork.common.constants.Messages;

public class NoAvailableResourcesException extends FogbowException {
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
