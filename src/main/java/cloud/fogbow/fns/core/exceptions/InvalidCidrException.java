package cloud.fogbow.fns.core.exceptions;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.constants.Messages;

public class InvalidCidrException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public InvalidCidrException(String cidr) {
        super(String.format(Messages.Exception.INVALID_CIDR, cidr));
    }

    public InvalidCidrException(String message, Throwable cause) {
        super(message, cause);
    }
}
