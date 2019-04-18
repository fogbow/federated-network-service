package cloud.fogbow.fns.constants;

import java.util.concurrent.TimeUnit;

public class ConfigurationPropertyDefaults {
    // FNS CONF DEFAULTS
    public static final String BUILD_NUMBER = "[testing mode]";
    public static final String XMPP_TIMEOUT = Long.toString(TimeUnit.SECONDS.toMillis(5));
    public static final String XMPP_JID_KEY = "xmpp_jid";
    public static final String XMPP_VLAN_ID_SERVICE_JID = "xmpp_vlan_id_service_jid";
    public static final String XMPP_PASSWORD_KEY = "xmpp_password";
    public static final String XMPP_SERVER_IP_KEY = "xmpp_server_ip";
    public static final String XMPP_C2C_PORT_KEY = "xmpp_c2c_port";
    public static final String XMPP_TIMEOUT_KEY = "xmpp_timeout";
}
