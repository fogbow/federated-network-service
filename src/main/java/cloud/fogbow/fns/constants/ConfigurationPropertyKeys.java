package cloud.fogbow.fns.constants;

import sun.management.resources.agent;

public class ConfigurationPropertyKeys {
    // FNS configuration
    public static final String XMPP_JID_KEY = "xmpp_jid";
    public static final String XMPP_PASSWORD_KEY = "xmpp_password";
    public static final String XMPP_SERVER_IP_KEY = "xmpp_server_ip";
    public static final String XMPP_C2C_PORT_KEY = "xmpp_c2c_port";
    public static final String XMPP_TIMEOUT_KEY = "xmpp_timeout";

    public static final String DFNS_LOCAL_AGENT_IP_ADDRESS_KEY = "dfns_local_agent_ip_address";

    public static final String BUILD_NUMBER_KEY = "build_number";

    // Plugins
    public static final String AUTHORIZATION_PLUGIN_CLASS_KEY = "authorization_plugin_class";

    // AS configuration
    public static final String AS_PORT_KEY = "as_port";
    public static final String AS_URL_KEY = "as_url";

    // RAS configuration
    public static final String RAS_PORT_KEY = "ras_port";
    public static final String RAS_URL_KEY = "ras_url";

    // Agent configuration
    public static final String ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "add_federated_network_script_path";
    public static final String FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY = "federated_network_agent_permission_file_path";
    public static final String FEDERATED_NETWORK_AGENT_USER_KEY = "federated_network_agent_user";
    public static final String FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY = "federated_network_agent_private_address";
    public static final String FEDERATED_NETWORK_AGENT_ADDRESS_KEY = "federated_network_agent_address";
    public static final String FEDERATED_NETWORK_PRE_SHARED_KEY_KEY = "federated_network_agent_pre_shared_key";
    public static final String REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "remove_federated_network_script_path";

    // DFNS Agent configuration
    public static final String CREATE_TUNNELS_SCRIPT_PATH = "create_federated_network_script_path";
    public static final String CREATE_TUNNEL_FROM_AGENT_TO_COMPUTE_SCRIPT_PATH = "create_tunnel_from_agent_to_compute_script_path";
}
