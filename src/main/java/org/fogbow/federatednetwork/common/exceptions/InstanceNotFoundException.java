package org.fogbow.federatednetwork.common.exceptions;

import org.fogbow.federatednetwork.common.constants.Messages;

public class InstanceNotFoundException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public InstanceNotFoundException() {
        super(Messages.Exception.INSTANCE_NOT_FOUND);
    }

    public InstanceNotFoundException(String message) {
        super(message);
    }

    public InstanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
