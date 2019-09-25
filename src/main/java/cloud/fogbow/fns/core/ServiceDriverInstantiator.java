package cloud.fogbow.fns.core;

import cloud.fogbow.common.util.HomeDir;
import cloud.fogbow.common.util.PropertiesUtil;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.drivers.ServiceDriver;

import java.io.File;
import java.util.Properties;

public class ServiceDriverInstantiator {
    private final String DRIVER_CLASS_NAME_KEY = "driver_class_name";

    public ServiceDriver getDriver(String serviceName) {
        String serviceClassName = getServiceProperties(serviceName).getProperty(DRIVER_CLASS_NAME_KEY);
        return (ServiceDriver) new ClassFactory().createInstance(serviceClassName);
    }

    private Properties getServiceProperties(String serviceName) {
        String path = HomeDir.getPath();
        return PropertiesUtil.readProperties(path +
                SystemConstants.SERVICES_DIRECTORY + File.separator + serviceName + File.separator +
                SystemConstants.DRIVER_CONF_FILE);
    }
}
