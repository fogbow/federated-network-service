package org.fogbow.federatednetwork.model;

public enum Operation {
    CREATE("create"),
    GET_ALL("getAll"),
    GET("get"),
    DELETE("delete");

    private String value;

    private Operation(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
