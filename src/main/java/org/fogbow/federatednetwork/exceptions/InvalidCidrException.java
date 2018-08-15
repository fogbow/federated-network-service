package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.FederatedNetworkConstants;

public class InvalidCidrException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidCidrException(String cidr) {
        super(String.format(FederatedNetworkConstants.INVALID_CIDR, cidr));
    }
}
