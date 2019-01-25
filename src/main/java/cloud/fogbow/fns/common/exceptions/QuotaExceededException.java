package cloud.fogbow.fns.common.exceptions;

import cloud.fogbow.fns.common.constants.Messages;

public class QuotaExceededException extends FogbowException {
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
