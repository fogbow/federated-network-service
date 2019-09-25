package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FatalErrorException;
import cloud.fogbow.common.util.HomeDir;
import cloud.fogbow.common.util.PropertiesUtil;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.model.ConfigurationMode;

import java.io.File;
import java.util.*;

public class PropertiesHolder {
    private static PropertiesHolder instance;
    private Map<String, Properties> properties;

    private PropertiesHolder() throws FatalErrorException {
        properties = new HashMap<>();
        String path = HomeDir.getPath();
        String serviceNames = PropertiesUtil.readProperties(path + SystemConstants.FNS_CONF_FILE).getProperty(ConfigurationPropertyKeys.SERVICE_NAMES_KEY);

        for(String serviceName : serviceNames.split(",")) {
            properties.put(serviceName, PropertiesUtil.readProperties(path + SystemConstants.SERVICES_DIRECTORY +
                File.separator + serviceName + File.separator + SystemConstants.DRIVER_CONF_FILE));
        }

        properties.put("fns", PropertiesUtil.readProperties(path + SystemConstants.FNS_CONF_FILE));
    }

    public static synchronized PropertiesHolder getInstance() throws FatalErrorException {
        if (instance == null) {
            instance = new PropertiesHolder();
        }
        return instance;
    }

    public String getProperty(String propertyName) {
        return properties.get("fns").getProperty(propertyName);
    }

    public String getProperty(String propertyName, String serviceName) {
        return this.properties.get(serviceName).getProperty(propertyName);
    }

    public String getPropertyOrDefault(String propertyName, String defaultValue) {
        String propertyValue = this.properties.get("fns").getProperty(propertyName, defaultValue);
        if (propertyValue.trim().isEmpty()) {
            propertyValue = defaultValue;
        }
        return propertyValue;
    }
}
