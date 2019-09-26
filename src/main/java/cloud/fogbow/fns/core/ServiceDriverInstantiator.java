package cloud.fogbow.fns.core;

import cloud.fogbow.fns.core.drivers.ServiceDriver;

public class ServiceDriverInstantiator {
    private final String DRIVER_CLASS_NAME_KEY = "driver_class_name";

    public ServiceDriver getDriver(String serviceName) {
        String serviceClassName = PropertiesHolder.getInstance().getProperty(DRIVER_CLASS_NAME_KEY, serviceName);
        return (ServiceDriver) new ClassFactory().createInstance(serviceClassName);
    }

    public ServiceDriver getDriver(String serviceName, String memberName) {
        String serviceClassName = PropertiesHolder.getInstance().getProperty(DRIVER_CLASS_NAME_KEY, serviceName);
        return (ServiceDriver) new ClassFactory().createInstance(serviceClassName, memberName);
    }
}
