package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class InvalidParameterException extends FogbowFnsException {
    private static final long serialVersionUID = 1L;

    public InvalidParameterException() {
        super(Messages.Exception.INVALID_PARAMETER);
    }

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
