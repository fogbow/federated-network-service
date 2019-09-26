package cloud.fogbow.fns.core.drivers.intercomponent.xmpp;

public enum RemoteMethod {
    REMOTE_CONFIGURE_MEMBER("remoteConfigureMember"),
    ACQUIRE_VLAN_ID("acquireVlanId"),
    REMOTE_RELEASE_VLAN_ID("releaseVlanId"),
    REMOTE_REMOVE_FEDNET("removeFederatedNetwork"),
    REMOTE_REMOVE_AGENT_TO_COMPUTE_TUNNEL("removeAgentToComputeTunnel"),
    REMOTE_ADD_INSTANCE_PUBLIC_KEY("addInstancePublicKey"),
    REMOTE_GET_REMOTE_AGENT_CONFIGURATION("getRemoteAgentConfiguration"),
    REMOTE_CONFIGURE_AGENT("remoteConfigureAgent");

    private final String namespace;

    RemoteMethod(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return namespace;
    }
}