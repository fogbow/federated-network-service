package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class AgentCommucationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AgentCommucationException() {
        super(Messages.Exception.UNABLE_TO_COMMUNICATE_WITH_AGENT);
    }
}
