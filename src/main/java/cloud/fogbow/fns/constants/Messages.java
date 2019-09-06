package cloud.fogbow.fns.constants;

public class Messages {
    public static class Exception {
        public static final String CONFIGURATION_MODE_NOT_IMPLEMENTED = "Configuration mode not implemented.";
        public static final String GENERIC_EXCEPTION = "Operation returned error: %s";
        public static final String INVALID_URL = "Please check the url %s";
        public static final String INVALID_CIDR = "Invalid CIDR (%s).";
        public static final String NO_MORE_IPS_AVAILABLE = "No more IPs available.";
        public static final String NO_MORE_VLAN_IDS_AVAILABLE = "No more Vlan IDs available.";
        public static final String ORDER_SHOULD_BE_CLOSED_BEFORE_DEACTIVATED = "Order '%s' should be closed before deactivated.";
        public static final String REQUESTER_DOES_NOT_OWN_REQUEST = "Requester does not own request.";
        public static final String THREAD_HAS_BEEN_INTERRUPTED = "Thread has been interrupted";
        public static final String UNABLE_TO_COMMUNICATE_WITH_AGENT = "Unable to communicate with agent.";
        public static final String UNABLE_TO_FIND_FEDERATED_NETWORK = "Unable to find federated network %s.";
        public static final String UNABLE_TO_FIND_LIST_FOR_REQUESTS = "Unable to find list for requests in state %s.";
        public static final String UNABLE_TO_LOAD_PUBLIC_KEY = "Unable to load FNS public key.";
        public static final String UNABLE_TO_REMOVE_FEDERATED_NETWORK = "Unable to remove federated network.";
        public static final String UNEXPECTED_EXCEPTION = "Unexpected exception.";
        public static final String UNABLE_TO_DESERIALIZE_SYSTEM_USER = "Unable to deserialize system user.";
    }

    public static class Fatal {
        public static final String UNABLE_TO_FIND_CLASS_S = "Unable to find class %s.";
    }

    public static class Warn {
        public static final String UNABLE_TO_RELEASE_VLAN_ID = "VLAN ID %s is not on allocated list, so unable to release it.";
        public static final String UNABLE_TO_DELETE_TUNNEL = "Unable to delete tunnel from agent to compute. Host IP: %s. VLAN ID: %s.";
    }

    public static class Info {
        public static final String CREATE_COMPUTE = "Create compute request: [%s]";
        public static final String CREATE_FEDERATED_NETWORK = "Create federated network request: %s";
        public static final String DELETE_COMPUTE = "Delete compute request received: [%s]";
        public static final String DELETE_FEDERATED_NETWORK = "Delete federated network with id: %s";
        public static final String DELETED_FEDERATED_NETWORK = "Successfully deleted federated network %s on agent.";
        public static final String DELETING_FEDERATED_NETWORK = "Deleting federated network: %s.";
        public static final String REDIRECT_REQUEST = "Request redirected to RAS";
        public static final String GET_COMPUTE_BY_ID = "Get compute request received: [%s]";
        public static final String GET_FEDERATED_NETWORK_BY_ID = "Get federated network with id: %s";
        public static final String GET_FEDERATED_NETWORK_STATUS = "Get federated network status request";
        public static final String GET_PUBLIC_KEY = "Get public key received.";
        public static final String GET_VERSION = "Get version request received.";
        public static final String STARTING_THREADS = "Starting threads.";
        public static final String NO_REMOTE_COMMUNICATION_CONFIGURED = "No remote communication configured.";
        public static final String INITIALIZING_DELETE_METHOD = "Initializing delete method, federated network id: %s.";
    }

    public static class Error {
        public static final String INVALID_CIDR = "Recovering and order with wrong CIDR.";
        public static final String NO_PACKET_SENDER = "No packet sender.";
        public static final String REQUEST_ALREADY_CLOSED = "Request %s is already in the closed state.";
        public static final String RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND = "Resource Allocation Service does not respond.";
        public static final String TRYING_TO_CREATE_AGENT_ERROR = "Trying to create agent with atts (%s). Error : %s.";
        public static final String TRYING_TO_CREATE_AGENT_OUTPUT = "Trying to create agent with atts (%s). Output : %s.";
        public static final String TRYING_TO_DELETE_AGENT_ERROR = "Trying to delete agent with atts (%s). Error : %s.";
        public static final String TRYING_TO_DELETE_AGENT_OUTPUT = "Trying to delete agent with atts (%s). Output : %s.";
        public static final String UNABLE_TO_CALL_AGENT = "Unable to call agent; process command: %s";
        public static final String UNABLE_TO_DELETE_AGENT = "Unable to delete agent; process command: %s";
    }
}
