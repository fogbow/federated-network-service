package org.fogbow.federatednetwork.common.exceptions;

import org.fogbow.federatednetwork.common.constants.Messages;

public class InvalidTokenException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public InvalidTokenException() {
        super(Messages.Exception.INVALID_TOKEN);
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

}
