package cloud.fogbow.fns.core.exceptions;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.constants.Messages;

public class AgentCommunicationException extends FogbowException {
    private static final long serialVersionUID = 1L;

    public AgentCommunicationException() {
        super(Messages.Exception.UNABLE_TO_COMMUNICATE_WITH_AGENT);
    }

    public AgentCommunicationException(String message) {
        super(message);
    }

    public AgentCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
