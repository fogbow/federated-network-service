package cloud.fogbow.fns.core.drivers.constants;

public class DriversConfigurationPropertyKeys {

    public static final String FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY = "federated_network_agent_permission_file_path";
    public static final String FEDERATED_NETWORK_AGENT_USER_KEY = "federated_network_agent_user";
    public static final String FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY = "federated_network_agent_private_address";
    public static final String FEDERATED_NETWORK_AGENT_ADDRESS_KEY = "federated_network_agent_address";
    public static final String FEDERATED_NETWORK_PRE_SHARED_KEY_KEY = "federated_network_agent_pre_shared_key";
    //this field must be set in all driver.conf
    public static final String DRIVER_CLASS_NAME_KEY = "driver_class_name";

    public static class Dfns {
        public static final String VLAN_ID_SERVICE_URL_KEY = "vlan_id_service_url";
        public static final String DEFAULT_NETWORK_CIDR_KEY = "default_network_cidr";
        public static final String LOCAL_MEMBER_NAME_KEY = "local_member_name";
        public static final String CREATE_TUNNEL_FROM_COMPUTE_TO_AGENT_SCRIPT_PATH_KEY = "create_tunnel_from_compute_to_agent_script_path";
        public static final String HOST_IP_KEY = "host_ip";
        public static final String FEDERATED_NETWORK_AGENT_USER_KEY = "federated_network_agent_user";
        public static final String FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY = "federated_network_agent_private_address";
        public static final String FEDERATED_NETWORK_AGENT_ADDRESS_KEY = "federated_network_agent_address";
        public static final String FEDERATED_NETWORK_PRE_SHARED_KEY_KEY = "federated_network_agent_pre_shared_key";
    }

    public static class Vanilla {
        public static final String ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "add_federated_network_script_path";
        public static final String REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "remove_federated_network_script_path";
        public static final String HOST_IP_KEY = "host_ip";
        public static final String FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY = "federated_network_agent_permission_file_path";
        public static final String FEDERATED_NETWORK_AGENT_USER_KEY = "federated_network_agent_user";
        public static final String FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY = "federated_network_agent_private_address";
        public static final String FEDERATED_NETWORK_AGENT_ADDRESS_KEY = "federated_network_agent_address";
        public static final String AGENT_SCRIPTS_PATH_KEY = "agent_scripts_path";
        public static final String FEDERATED_NETWORK_PRE_SHARED_KEY_KEY = "federated_network_agent_pre_shared_key";
    }
}
