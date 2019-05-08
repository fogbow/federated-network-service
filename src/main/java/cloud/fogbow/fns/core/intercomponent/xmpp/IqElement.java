package cloud.fogbow.fns.core.intercomponent.xmpp;

public enum IqElement {
    QUERY("query"),
    FEDERATED_NETWORK_ORDER("order"),
    COMPUTE_ORDER("computeOrder"),
    MEMBER_CONFIGURATION_STATE("memberConfigurationState"),
    VLAN_ID("vlanId"),
    VLAN_ID_CLASS_NAME("vlanIdClassName"),
    HOST_IP("hostIp"),
    INSTANCE_PUBLIC_KEY("instancePublicKey"),
    DFNS_AGENT_CONFIGURATION("dfngAgentConfiguration"),
    DFNS_AGENT_CONFIGURATION_CLASS("dfnsAgentConfigurationClass");

    private final String element;

    IqElement(final String elementName) {
        this.element = elementName;
    }

    @Override
    public String toString() {
        return element;
    }
}