package org.fogbow.federatednetwork.common.exceptions;

import org.fogbow.federatednetwork.common.constants.Messages;

public class InvalidParameterException extends FogbowException {
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
