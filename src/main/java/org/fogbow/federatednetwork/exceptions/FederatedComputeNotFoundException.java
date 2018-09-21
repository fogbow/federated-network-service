package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class FederatedComputeNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public FederatedComputeNotFoundException(String id) {
        super(String.format(Messages.Exception.UNABLE_TO_FIND_FEDERATED_COMPUTE, id));
    }

}
