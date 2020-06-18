package cloud.fogbow.fns.constants;

public class Messages {
    public static class Exception {
        public static final String GENERIC_EXCEPTION_S = "Operation returned error: %s.";
        public static final String INVALID_URL_S = "Please check the url %s.";
        public static final String INVALID_CIDR_S = "Invalid CIDR (%s).";
        public static final String NO_MORE_IPS_AVAILABLE = Log.NO_MORE_IPS_AVAILABLE;
        public static final String NO_SERVICE_SPECIFIED = "No service has been specified.";
        public static final String NOT_SUPPORTED_SERVICE_S = "The service %s is not supported yet.";
        public static final String ORDER_S_SHOULD_BE_CLOSED_BEFORE_DEACTIVATED = "Order '%s' should be closed before deactivated.";
        public static final String REQUESTER_DOES_NOT_OWN_REQUEST = "Requester does not own request.";
        public static final String RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND = "Resource Allocation Service does not respond.";
        public static final String UNABLE_TO_CALL_AGENT = Log.UNABLE_TO_CALL_AGENT_S;
        public static final String UNABLE_TO_DELETE_AGENT = Log.UNABLE_TO_DELETE_AGENT_S;
        public static final String UNABLE_TO_COPY_FILE_REMOTELY = Log.UNABLE_TO_COPY_FILE_REMOTELY;
        public static final String UNABLE_TO_DESERIALIZE_SYSTEM_USER = "Unable to deserialize system user.";
        public static final String UNABLE_TO_FIND_CLASS_S = "Unable to find class %s.";
        public static final String UNABLE_TO_FIND_FEDERATED_NETWORK_S = "Unable to find federated network %s.";
        public static final String UNABLE_TO_FIND_LIST_FOR_REQUESTS_IN_STATE_S = "Unable to find list for requests in state %s.";
        public static final String UNABLE_TO_LOAD_PUBLIC_KEY = "Unable to load FNS public key.";
        public static final String UNABLE_TO_REMOVE_FEDERATED_NETWORK = "Unable to remove federated network.";
        public static final String UNEXPECTED_EXCEPTION = "Unexpected exception.";
    }

    public static class Log {
        public static final String CREATE_COMPUTE_REQUEST_S = "Create compute request: %s.";
        public static final String CREATE_FEDERATED_NETWORK_REQUEST_S = "Create federated network request: %s.";
        public static final String DELETE_COMPUTE_REQUEST_S = "Delete compute request: %s.";
        public static final String DELETE_FEDERATED_NETWORK_S = "Delete federated network with id: %s.";
        public static final String DELETING_FEDERATED_NETWORK_S = "Deleting federated network: %s.";
        public static final String GET_COMPUTE_BY_ID_S = "Get compute request: %s.";
        public static final String GET_FEDERATED_NETWORK_WITH_ID_S = "Get federated network with id: %s.";
        public static final String GET_PUBLIC_KEY = "Get public key received.";
        public static final String NO_MORE_IPS_AVAILABLE = "No more IPs available.";
        public static final String STARTING_THREADS = "Starting threads.";
        public static final String INITIALIZING_DELETE_METHOD_S = "Initializing delete method, federated network id: %s.";
        public static final String RECEIVING_GET_SERVICES_REQUEST = "Receiving get services request.";
        public static final String INVALID_CIDR = "Recovering and order with invalid CIDR.";
        public static final String REQUEST_S_ALREADY_CLOSED = "Request %s is already in the closed state.";
        public static final String UNABLE_TO_CALL_AGENT_S = "Unable to call agent; process command: %s.";
        public static final String UNABLE_TO_DELETE_AGENT_S = "Unable to delete agent; process command: %s.";
        public static final String UNABLE_TO_COPY_FILE_REMOTELY = "Unable to copy file remotely.";
        public static final String THREAD_HAS_BEEN_INTERRUPTED = "Thread has been interrupted.";
    }
}
