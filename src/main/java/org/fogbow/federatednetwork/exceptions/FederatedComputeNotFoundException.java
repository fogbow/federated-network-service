package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.FederatedNetworkConstants;

public class FederatedComputeNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public FederatedComputeNotFoundException(String id) {
        super(FederatedNetworkConstants.NOT_FOUND_FEDERATED_COMPUTE + id);
    }

}
