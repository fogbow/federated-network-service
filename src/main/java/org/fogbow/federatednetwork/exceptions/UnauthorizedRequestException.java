package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class UnauthorizedRequestException extends FogbowFnsException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedRequestException() {
        super(Messages.Exception.AUTHORIZATION_ERROR);
    }

    public UnauthorizedRequestException(String message) {
        super(message);
    }

    public UnauthorizedRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
