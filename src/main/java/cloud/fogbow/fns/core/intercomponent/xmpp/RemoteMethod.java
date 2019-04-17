package cloud.fogbow.fns.core.intercomponent.xmpp;

public enum RemoteMethod {
    REMOTE_CONFIGURE_MEMBER("remoteConfigureMember"),
    REMOTE_GET_FREE_VLAN_ID("getFreeVlanId"),
    REMOTE_RELEASE_VLAN_ID("releaseVlanId");

    private final String namespace;

    RemoteMethod(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return namespace;
    }
}