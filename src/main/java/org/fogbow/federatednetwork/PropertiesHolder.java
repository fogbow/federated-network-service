package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.common.exceptions.FatalErrorException;
import org.fogbow.federatednetwork.common.util.HomeDir;
import org.fogbow.federatednetwork.common.util.PropertiesUtil;
import org.fogbow.federatednetwork.constants.SystemConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesHolder {
    private Properties properties;
    private static PropertiesHolder instance;

    private PropertiesHolder() throws FatalErrorException {
        String path = HomeDir.getPath();
        List<String> configFilesNames = new ArrayList<>();
        configFilesNames.add(path + SystemConstants.FNS_CONF_FILE);
        this.properties = PropertiesUtil.readProperties(configFilesNames);
    }

    public static synchronized PropertiesHolder getInstance() throws FatalErrorException {
        if (instance == null) {
            instance = new PropertiesHolder();
        }
        return instance;
    }

    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    public String getProperty(String propertyName, String defaultPropertyValue) {
        String propertyValue = this.properties.getProperty(propertyName, defaultPropertyValue);
        if (propertyValue.trim().isEmpty()) {
            propertyValue = defaultPropertyValue;
        }
        return propertyValue;
    }

    public Properties getProperties() {
        return this.properties;
    }
}
