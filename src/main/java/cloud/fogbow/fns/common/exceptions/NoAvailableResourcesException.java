package cloud.fogbow.fns.common.exceptions;

import cloud.fogbow.fns.common.constants.Messages;

public class NoAvailableResourcesException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public NoAvailableResourcesException() {
        super(Messages.Exception.NO_AVAILABLE_RESOURCES);
    }

    public NoAvailableResourcesException(String message) {
        super(message);
    }

    public NoAvailableResourcesException(String message, Throwable cause) {
        super(message, cause);
    }

}
