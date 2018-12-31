package org.fogbow.federatednetwork.exceptions;

import org.fogbow.federatednetwork.constants.Messages;

public class AgentCommucationException extends FogbowFnsException {
    private static final long serialVersionUID = 1L;

    public AgentCommucationException() {
        super(Messages.Exception.UNABLE_TO_COMMUNICATE_WITH_AGENT);
    }

    public AgentCommucationException(String message) {
        super(message);
    }

    public AgentCommucationException(String message, Throwable cause) {
        super(message, cause);
    }
}
