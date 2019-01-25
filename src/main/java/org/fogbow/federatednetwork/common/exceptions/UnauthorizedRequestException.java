package org.fogbow.federatednetwork.common.exceptions;

import org.fogbow.federatednetwork.common.constants.Messages;

public class UnauthorizedRequestException extends FogbowException {
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
