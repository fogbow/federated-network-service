package cloud.fogbow.fns.common.exceptions;

import cloud.fogbow.fns.common.constants.Messages;

public class FatalErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FatalErrorException() {
        super(Messages.Exception.FATAL_ERROR);
    }

    public FatalErrorException(String message) {
        super(message);
    }

    public FatalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
