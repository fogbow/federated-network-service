package cloud.fogbow.fns.core.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ConfigurationMode {
    VANILLA("vanilla"), DFNS("dfns");


    private final String value;

    ConfigurationMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
