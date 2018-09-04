package org.fogbow.federatednetwork.model;

public enum FederatedResourceType {
    FEDERATED_COMPUTE("federated-compute"),
    FEDERATED_NETWORK("federated-network");

    private String value;

    private FederatedResourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
