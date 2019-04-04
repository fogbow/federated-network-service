package cloud.fogbow.fns.core.intercomponent.xmpp;

public enum RemoteMethod {
    REMOTE_CONFIGURE_MEMBER("remoteConfigureMember");

    private final String namespace;

    RemoteMethod(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return namespace;
    }
}