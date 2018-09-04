package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.FederatedNetworkConstants;

public class AgentCommucationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AgentCommucationException() {
        super(FederatedNetworkConstants.ERROR_WHILE_TRYING_TO_COMMUNICATE_WITH_AGENT);
    }
}
