package cloud.fogbow.fns.exceptions;

import cloud.fogbow.fns.constants.Messages;

public class InvalidCidrException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidCidrException(String cidr) {
        super(String.format(Messages.Exception.INVALID_CIDR, cidr));
    }

    public InvalidCidrException(String message, Throwable cause) {
        super(message, cause);
    }
}
