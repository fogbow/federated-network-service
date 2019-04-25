package cloud.fogbow.fns.core.intercomponent.xmpp;

public enum RemoteMethod {
    REMOTE_CONFIGURE_MEMBER("remoteConfigureMember"),
    REMOTE_GET_VLAN_ID("getVlanId"),
    REMOTE_RELEASE_VLAN_ID("releaseVlanId"),
    REMOTE_REMOVE_FEDNET("removeFederatedNetwork"),
    REMOTE_REMOVE_AGENT_TO_COMPUTE_TUNNEL("removeAgentToComputeTunnel");

    private final String namespace;

    RemoteMethod(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return namespace;
    }
}