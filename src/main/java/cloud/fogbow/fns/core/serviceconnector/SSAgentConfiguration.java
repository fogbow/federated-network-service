package cloud.fogbow.fns.core.serviceconnector;

public class SSAgentConfiguration implements AgentConfiguration {
    private String defaultNetworkCidr;
    private String agentUser;
    private String publicKey;
    private String privateKey;

    private String privateIpAddress;
    private String publicIpAddress;

    public SSAgentConfiguration(String defaultNetworkCidr, String agentUser, String publicKey, String privateIpAddress, String publicIpAddress) {
        this.defaultNetworkCidr = defaultNetworkCidr;
        this.agentUser = agentUser;
        this.publicKey = publicKey;
        this.privateIpAddress = privateIpAddress;
        this.publicIpAddress = publicIpAddress;
    }

    public SSAgentConfiguration(String defaultNetworkCidr, String agentUser, String privateIpAddress, String publicIpAddress) {
        this.defaultNetworkCidr = defaultNetworkCidr;
        this.agentUser = agentUser;
        this.privateIpAddress = privateIpAddress;
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

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
