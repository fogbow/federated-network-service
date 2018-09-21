package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class InvalidCidrException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidCidrException(String cidr) {
        super(String.format(Messages.Exception.INVALID_CIDR, cidr));
    }
}
