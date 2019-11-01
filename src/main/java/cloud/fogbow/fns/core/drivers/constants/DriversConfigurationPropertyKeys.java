package cloud.fogbow.fns.core.drivers.constants;

public class DriversConfigurationPropertyKeys {

    public static final String FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY = "federated_network_agent_permission_file_path";
    public static final String FEDERATED_NETWORK_AGENT_USER_KEY = "federated_network_agent_user";
    public static final String FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY = "federated_network_agent_private_address";
    public static final String FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY = "federated_network_agent_public_address";
    public static final String AGENT_SCRIPTS_PATH_KEY = "agent_scripts_path";
    //this field must be set in every driver.conf
    public static final String DRIVER_CLASS_NAME_KEY = "driver_class_name";

    public static class Dfns {
        public static final String VLAN_ID_SERVICE_URL_KEY = "vlan_id_service_url";
        public static final String DEFAULT_NETWORK_CIDR_KEY = "default_network_cidr";
        public static final String CREATE_TUNNEL_FROM_COMPUTE_TO_AGENT_SCRIPT_PATH_KEY = "create_tunnel_from_compute_to_agent_script_path";
    }

    public static class Vanilla {
        public static final String FEDERATED_NETWORK_PRE_SHARED_KEY_KEY = "federated_network_agent_pre_shared_key";
        public static final String ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "add_federated_network_script_path";
        public static final String REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "remove_federated_network_script_path";
    }
}
