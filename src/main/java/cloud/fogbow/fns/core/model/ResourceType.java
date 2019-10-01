package cloud.fogbow.fns.core.model;

public enum ResourceType {
    FEDERATED_NETWORK("federated-network"),
    SERVICE_NAMES("service-names");

    private String value;

    private ResourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
