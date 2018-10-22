package org.fogbow.federatednetwork.constants;

public class Messages {
    public static class Exception {
        public static final String INVALID_CIDR = "Invalid CIDR (%s).";
        public static final String NO_FREE_IPS_LEFT = "No free IPs left.";
        public static final String UNABLE_TO_COMMUNICATE_WITH_AGENT = "Unable to communicate with agent.";
        public static final String UNABLE_TO_FIND_FEDERATED_COMPUTE = "Unable to find federated compute %s.";
        public static final String UNABLE_TO_FIND_FEDERATED_NETWORK = "Unable to find federated network %s.";
        public static final String UNABLE_TO_REMOVE_FEDERATED_NETWORK = "Unable to remove federated network.";
    }

    public static class Fatal {
        public static final String ERROR_LOADING_PROPERTIES = "Error loading properties.";
    }

    public static class Warn {
        public static final String ERROR_READING_CONF_FILE = "Error trying to read configuration file: %s.";
        public static final String ERROR_CLOSING_CONF_FILE = "Error trying to close configuration file: %s.";
    }

    public static class Info {
        public static final String DELETED_FEDERATED_NETWORK = "Successfully deleted federated network %s on agent.";
        public static final String DELETING_FEDERATED_NETWORK = "Deleting federated network: %s.";
        public static final String GET_VERSION_REQUEST_RECEIVED = "Get version request received.";
        public static final String INITIALIZING_DELETE_METHOD = "Initializing delete method, user: %s, federated network id: %s.";
    }

    public static class Error {
        public static final String FEDERATED_NETWORK_IN_USE = "Federated network is currently in use.";
        public static final String RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND = "Resource Allocation Service does not respond.";public static final String TRYING_TO_CREATE_AGENT_ERROR = "Trying to create agent with atts (%s). Error : %s.";
        public static final String TRYING_TO_CREATE_AGENT_OUTPUT = "Trying to create agent with atts (%s). Output : %s.";
        public static final String TRYING_TO_DELETE_AGENT_ERROR = "Trying to delete agent with atts (%s). Error : %s.";
        public static final String TRYING_TO_DELETE_AGENT_OUTPUT = "Trying to delete agent with atts (%s). Output : %s.";
        public static final String UNABLE_TO_ADD_TIMESTAMP = "Unable to add timestamp.";
        public static final String UNABLE_TO_CALL_AGENT = "Unable to call agent; process command: %s";
        public static final String UNABLE_TO_CREATE_TIMESTAMP_TABLE = "Unable to create timestamp table.";
        public static final String UNABLE_TO_DELETE_AGENT = "Unable to delete agent; process command: %s";
        public static final String UNABLE_TO_FIND_FEDERATED_NETWORK = "Unable to find federated network.";
        public static final String UNABLE_TO_ROLLBACK_TRANSACTION = "Unable to rollback transaction.";
    }
}
