package cloud.fogbow.fns.core.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;

@JsonDeserialize(using = ConfigurationModeDeserializer.class)
public enum ConfigurationMode {
    VANILLA("vanilla"),
    DFNS("dfns");

    private String value;

    ConfigurationMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConfigurationMode fromString(String modeString) {
        for (ConfigurationMode mode : ConfigurationMode.values()) {
            if (mode.getValue().equals(modeString)) {
                return mode;
            }
        }
        return null;
    }
}

class ConfigurationModeDeserializer extends JsonDeserializer<ConfigurationMode> {

    @Override
    public ConfigurationMode deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        if (node == null) {
            return null;
        }

        return ConfigurationMode.fromString(node.textValue());
    }

}