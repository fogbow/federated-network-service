package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.ConfigurationConstants;
import org.fogbow.federatednetwork.constants.Messages;

public class SubnetAddressesCapacityReachedException extends FogbowFnsException {
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
