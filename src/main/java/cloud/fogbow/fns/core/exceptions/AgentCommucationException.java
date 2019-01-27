package cloud.fogbow.fns.core.exceptions;

import cloud.fogbow.fns.core.constants.Messages;

public class AgentCommucationException extends Exception {
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
