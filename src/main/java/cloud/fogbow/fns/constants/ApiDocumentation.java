package cloud.fogbow.fns.constants;

public class ApiDocumentation {
    public static class FederatedNetwork {
        public static final String API = "Creates a federated network spanning multiple cloud providers.";
        public static final String CREATE_OPERATION = "Creates a federated network instance.";
        public static final String CREATE_REQUEST_BODY = "The parameters for the creation of a federated network are\n" +
                "the list of IDs of the providers that will be connected, the CIDR of the network, and the name\n" +
                "that will be given to the federated network.";
        public static final String GET_OPERATION = "Lists all federated networks created by the user.";
        public static final String GET_BY_ID_OPERATION = "Lists a specific federated network.";
        public static final String ID = "The ID of the specific federated network.";
        public static final String DELETE_OPERATION = "Deletes a specific federated network.";
    }

    public static class Compute {
        public static final String API = "Manages compute instances.";
        public static final String CREATE_OPERATION = "Creates a compute instance.";
        public static final String CREATE_REQUEST_BODY =
                "The body of the request must specify the amount of vCpus (vCPU), the amount of memory (in MB)\n" +
                        "(memory), the disk size in GB (disk) that is required, the image id (imageId) that is going to\n" +
                        "be used to create the compute, and optionally, the provider (provider) where the compute should\n" +
                        "be created, a name (name) that can be assigned to the compute, an array of private network ids\n" +
                        "(networkIds) that were previously created using RAS, the public key (publicKey) that is going to\n" +
                        "be used to allow remote connection to the compute, and information (userData) for the customization\n" +
                        "of the compute through the cloudinit mechanism.";
        public static final String GET_BY_ID_OPERATION = "Lists a specific compute.";
        public static final String ID = "The ID of the specific compute.";
        public static final String DELETE_OPERATION = "Deletes a specific compute.";
    }

    public static class Version {
        public static final String API = "Queries the version of the service's API.";
        public static final String GET_OPERATION = "Returns the version of the API.";
    }

    public static class PublicKey {
        public static final String API = "Queries the public key of the service.";
        public static final String GET_OPERATION = "Returns the public key of the service.";
    }

    public static class CommonParameters {
        public static final String FEDERATION_TOKEN = "This is the token that identifies a federation user.\n" +
                "It is typically created via a call to the /tokens endpoint of a Resource Allocation Service (RAS).";
    }
}
