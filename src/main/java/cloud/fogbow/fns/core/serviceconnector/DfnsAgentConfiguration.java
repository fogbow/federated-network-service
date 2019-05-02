package cloud.fogbow.fns.core.serviceconnector;

public class DfnsAgentConfiguration {
    private String defaultNetworkCidr;
    private String agentUser;
    private String publicKey;

    public DfnsAgentConfiguration(String defaultNetworkCidr, String agentUser, String publicKey) {
        this.defaultNetworkCidr = defaultNetworkCidr;
        this.agentUser = agentUser;
        this.publicKey = publicKey;
    }

    public String getDefaultNetworkCidr() {
        return defaultNetworkCidr;
    }

    public String getAgentUser() {
        return agentUser;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
