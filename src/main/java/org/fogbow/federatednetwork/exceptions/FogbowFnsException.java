package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class FogbowFnsException extends Exception {
    private static final long serialVersionUID = 1L;

    public FogbowFnsException() {
        super(Messages.Exception.FOGBOW_FNS);
    }

    public FogbowFnsException(String message) {
        super(message);
    }

    public FogbowFnsException(String message, Throwable cause) {
        super(message, cause);
    }
}
