package org.fogbow.federatednetwork.model;

public enum FederatedResourceType {

    FEDERATED_NETWORK("federatedNetwork"),
    FEDERATED_COMPUTE("federatedCompute");

    private String value;

    private FederatedResourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
