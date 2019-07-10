package cloud.fogbow.fns.core.exceptions;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.constants.Messages;

public class NoVlanIdsLeftException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public NoVlanIdsLeftException() {
        super(Messages.Exception.NO_MORE_VLAN_IDS_AVAILABLE);
    }

    public NoVlanIdsLeftException(String message) {
        super(message);
    }

    public NoVlanIdsLeftException(String message, Throwable cause) {
        super(message, cause);
    }
}
