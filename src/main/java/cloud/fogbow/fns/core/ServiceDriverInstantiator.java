package cloud.fogbow.fns.core;

import cloud.fogbow.common.util.HomeDir;
import cloud.fogbow.common.util.PropertiesUtil;
import cloud.fogbow.fns.core.drivers.ServiceDriver;

import java.io.File;
import java.util.Properties;

public class ServiceDriverInstantiator {

    public ServiceDriver getDriver(String serviceName) {
        String serviceClassName = getServiceProperties(serviceName).getProperty("driver_class_name");
        return (ServiceDriver) new ClassFactory().createInstance(serviceClassName);
    }

    private Properties getServiceProperties(String serviceName) {
        String path = HomeDir.getPath();
        return PropertiesUtil.readProperties(path +
                "services"+ File.separator + serviceName + File.separator +
                "driver.conf");
    }
}
