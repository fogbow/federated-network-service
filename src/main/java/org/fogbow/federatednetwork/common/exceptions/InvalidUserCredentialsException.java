package org.fogbow.federatednetwork.common.exceptions;

import org.fogbow.federatednetwork.common.constants.Messages;

public class InvalidUserCredentialsException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public InvalidUserCredentialsException() {
        super(Messages.Exception.INVALID_CREDENTIALS);
    }

    public InvalidUserCredentialsException(String message) {
        super(message);
    }

    public InvalidUserCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
