package cloud.fogbow.fns.core.exceptions;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.constants.Messages;

public class FederatedNetworkNotFoundException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public FederatedNetworkNotFoundException(String id) {
        super(String.format(Messages.Exception.UNABLE_TO_FIND_FEDERATED_NETWORK, id));
    }

    public FederatedNetworkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
