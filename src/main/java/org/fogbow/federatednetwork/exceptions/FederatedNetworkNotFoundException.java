package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.FederatedNetworkConstants;

public class FederatedNetworkNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public FederatedNetworkNotFoundException(String id) {
        super(FederatedNetworkConstants.NOT_FOUND_FEDERATED_NETWORK_MESSAGE + id);
    }

}
