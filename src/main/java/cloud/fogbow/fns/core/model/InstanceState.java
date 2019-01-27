package cloud.fogbow.fns.core.model;

public enum InstanceState {
    READY("ready"),
    FAILED("failed");

    private String value;

    private InstanceState(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
