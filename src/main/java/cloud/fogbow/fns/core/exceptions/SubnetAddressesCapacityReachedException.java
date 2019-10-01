package cloud.fogbow.fns.core.exceptions;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.constants.Messages;

public class SubnetAddressesCapacityReachedException extends FogbowException {
	private static final long serialVersionUID = 1L;

	public SubnetAddressesCapacityReachedException() {
		super(Messages.Exception.NO_MORE_IPS_AVAILABLE);
	}

	public SubnetAddressesCapacityReachedException(String message) {
		super(message);
	}

	public SubnetAddressesCapacityReachedException(String message, Throwable cause) {
		super(message, cause);
	}
}
