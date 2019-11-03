package cloud.fogbow.fns.core.drivers.intercomponent.xmpp;

public enum IqElement {
    QUERY("query"),
    FEDERATED_NETWORK_ORDER("order"),
    PROVIDER_ID("providerId"),
    HOST_IP("hostIp"),
    INSTANCE_PUBLIC_KEY("instancePublicKey"),
    REMOTE_AGENT_CONFIGURATION("remoteAgentConfiguration"),
    SERVICE_NAME("serviceName");

    private final String element;

    IqElement(final String elementName) {
        this.element = elementName;
    }

    @Override
    public String toString() {
        return element;
    }
}