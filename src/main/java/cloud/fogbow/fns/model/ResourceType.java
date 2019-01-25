package cloud.fogbow.fns.model;

public enum ResourceType {
    FEDERATED_NETWORK("federated-network");

    private String value;

    private ResourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
