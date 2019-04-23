package cloud.fogbow.fns.core.intercomponent.xmpp;

public enum IqElement {
    QUERY("query"),
    FEDERATED_NETWORK_ORDER("order"),
    MEMBER_CONFIGURATION_STATE("memberConfigurationState"),
    VLAN_ID("vlanId"),
    VLAN_ID_CLASS_NAME("vlanIdClassName");

    private final String element;

    IqElement(final String elementName) {
        this.element = elementName;
    }

    @Override
    public String toString() {
        return element;
    }
}