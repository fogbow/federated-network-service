package cloud.fogbow.fns.core.intercomponent.xmpp;

public enum IqElement {
    QUERY("query"),
    FEDERATED_NETWORK_ORDER("order");

    private final String element;

    IqElement(final String elementName) {
        this.element = elementName;
    }

    @Override
    public String toString() {
        return element;
    }
}