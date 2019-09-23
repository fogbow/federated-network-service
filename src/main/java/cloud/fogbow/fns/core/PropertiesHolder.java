package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FatalErrorException;
import cloud.fogbow.common.util.HomeDir;
import cloud.fogbow.common.util.PropertiesUtil;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.model.ConfigurationMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesHolder {
    private Properties generalProperties;
    private Properties dfnsProperties;
    private Properties vanillaProperties;
    private static PropertiesHolder instance;

    private PropertiesHolder() throws FatalErrorException {
        String path = HomeDir.getPath();
        this.generalProperties = PropertiesUtil.readProperties(path + SystemConstants.FNS_CONF_FILE);
        this.dfnsProperties = PropertiesUtil.readProperties(path + SystemConstants.DFNS_CONF_FILE);
        this.vanillaProperties = PropertiesUtil.readProperties(path + SystemConstants.VANILLA_CONF_FILE);
    }

    public static synchronized PropertiesHolder getInstance() throws FatalErrorException {
        if (instance == null) {
            instance = new PropertiesHolder();
        }
        return instance;
    }

    public String getProperty(String propertyName) {
        return generalProperties.getProperty(propertyName);
    }

    public String getProperty(String propertyName, String defaultPropertyValue) {
        String propertyValue = this.generalProperties.getProperty(propertyName, defaultPropertyValue);
        if (propertyValue.trim().isEmpty()) {
            propertyValue = defaultPropertyValue;
        }
        return propertyValue;
    }

    public String getProperty(String propertyName, ConfigurationMode configurationMode) {
        switch (configurationMode) {
            case VANILLA:
                return vanillaProperties.getProperty(propertyName);
            case DFNS:
                return dfnsProperties.getProperty(propertyName);
            default:
                return generalProperties.getProperty(propertyName);
        }
    }

    public Properties getProperties() {
        return this.generalProperties;
    }
}
