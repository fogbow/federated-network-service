package cloud.fogbow.fns.common.exceptions;

import cloud.fogbow.fns.common.constants.Messages;

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
