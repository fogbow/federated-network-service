package cloud.fogbow.fns.core.drivers.constants;

public class DriversConfigurationPropertyKeys {
    //this field must be set in every driver.conf
    public static final String DRIVER_CLASS_NAME_KEY = "driver_class_name";

    public static class Vanilla {
        public static final String FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY = "federated_network_agent_permission_file_path";
        public static final String FEDERATED_NETWORK_AGENT_USER_KEY = "federated_network_agent_user";
        public static final String FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY = "federated_network_agent_private_address";
        public static final String FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY = "federated_network_agent_public_address";
        public static final String AGENT_SCRIPTS_PATH_KEY = "agent_scripts_path";
        public static final String FEDERATED_NETWORK_PRE_SHARED_KEY_KEY = "federated_network_agent_pre_shared_key";
    }
}
