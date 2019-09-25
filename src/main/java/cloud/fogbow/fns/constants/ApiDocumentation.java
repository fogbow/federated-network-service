package cloud.fogbow.fns.constants;

public class ApiDocumentation {
    public static class ApiInfo {
        public static final String API_TITLE = "Fogbow Federated Network Service (FNS) API";
        public static final String API_DESCRIPTION = "This API allows the creation of " +
                "federated networks spanning multiple cloud providers. It also allows the creation of compute " +
                "instances that can be attached to the federated networks created. The FNS also works as a proxy " +
                "for its companion RAS service, forwarding requests that it cannot handle to the underlying RAS. " +
                "Users should refer to the RAS API documentation to get information on the extra calls that the " +
                "FNS is able to handle (essentially, all calls that can be made to the RAS API).";
    }

    public static class FederatedNetwork {
        public static final String API = "Creates a federated network spanning multiple cloud providers.";
        public static final String CREATE_OPERATION = "Creates a federated network instance.";
        public static final String CREATE_REQUEST_BODY = "The parameters for the creation of a federated network are " +
                "the list of IDs of the providers that will be connected, the CIDR of the network, the name " +
                "that will be given to the federated network, and the service mode. Currently, there are two " +
                "options available: vanilla (the default), and dfns.";
        public static final String GET_OPERATION = "Lists all federated networks created by the user.";
        public static final String GET_BY_ID_OPERATION = "Lists a specific federated network.";
        public static final String ID = "The ID of the specific federated network.";
        public static final String DELETE_OPERATION = "Deletes a specific federated network.";
    }

    public static class Compute {
        public static final String CREATE_REQUEST_BODY =
                "The body of the request has two parts. The first is an optional field that can be used " +
                "to specify the ID of a federated network that has been previously created, and to which " +
                "the compute to be created should be attached.\n\n The second part specifies the parameters " +
                "for the compute to be created. " + cloud.fogbow.ras.constants.ApiDocumentation.Compute.CREATE_REQUEST_BODY;
    }

    public static class Model {
        public static final String INSTANCE_ID = "9632af26-72ee-461a-99a9-1e5d59076a98";
        public static final String INSTANCE_NAME = "instance name";
        public static final String PROVIDERS = "[\"provider1.domain1\", \"provider2.domain2\"]";
        public static final String REQUESTER = "requester.domain";
        public static final String PROVIDER = "provider.domain";
        public static final String COMPUTE_LIST = "[\n" +
                "    {\"computeId\": \"64ee4355-4d7f-4170-80b4-5e8348af6a61\", \"ip\": \"10.10.0.2\"},\n" +
                "    {\"computeId\": \"0b0246e3-85ea-4642-93d5-f2c4fbd415d2\", \"ip\": \"10.10.0.3\"}\n" +
                "  ]";
        public static final String CIDR = "10.10.0.0/16";
        public static final String IP = "188.140.0.5";
        public static final String IP_NOTE = "(the IPs assigned to the compute)";
        public static final String DFNS_EXAMPLE = "dfns";
        public static final String SERVICE_LIST = "[{\"serviceNames:\" \"[dfns, vanilla]\"}]";
    }

    public static class Service {
        public static final String API = "Handle operations related to the available services";
        public static final String GET_OPERATION = "Returns the service names available in the site";
    }
}
