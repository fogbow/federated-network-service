package cloud.fogbow.fns.core.model;

public enum InstanceState {
    READY("ready"),
    FAILED("failed"),
    OPEN("open");

    private String value;

    InstanceState(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
