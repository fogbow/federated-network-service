package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class QuotaExceededException extends FogbowFnsException {
    private static final long serialVersionUID = 1L;

    public QuotaExceededException() {
        super(Messages.Exception.QUOTA_EXCEEDED);
    }

    public QuotaExceededException(String message) {
        super(message);
    }

    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
