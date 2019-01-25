package cloud.fogbow.fns.common.exceptions;

import cloud.fogbow.fns.common.constants.Messages;

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
