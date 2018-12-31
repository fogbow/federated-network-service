package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class UnauthorizedOperationException extends FogbowFnsException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedOperationException() {
        super(Messages.Exception.UNAUTHORIZED_OPERATION);
    }

    public UnauthorizedOperationException(String message) {
        super(message);
    }

    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
