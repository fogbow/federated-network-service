package cloud.fogbow.fns.core.serviceconnector;

public class DfnsAgentConfiguration {
    private String defaultNetworkCidr;
    private String agentUser;
    private String publicKey;

    private String agentPrivateIpAddress;
    private String publicIpAddress;

    public DfnsAgentConfiguration(String defaultNetworkCidr, String agentUser, String publicKey, String agentPrivateIpAddress, String publicIpAddress) {
        this.defaultNetworkCidr = defaultNetworkCidr;
        this.agentUser = agentUser;
        this.publicKey = publicKey;
        this.agentPrivateIpAddress = agentPrivateIpAddress;
        this.publicIpAddress = publicIpAddress;
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

    public String getAgentPrivateIpAddress() {
        return agentPrivateIpAddress;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }
}
