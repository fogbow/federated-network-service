package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FatalErrorException;
import cloud.fogbow.common.util.HomeDir;
import cloud.fogbow.common.util.PropertiesUtil;
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
        List<String> serviceNames = new ServiceListController().getServiceNames();

        for(String serviceName : serviceNames) {
            properties.put(serviceName, PropertiesUtil.readProperties(path + "services"+ File.separator + serviceName + File.separator + "driver.conf"));
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
}
