package cloud.fogbow.fns.core;

import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyKeys;
import cloud.fogbow.fns.core.drivers.ServiceDriver;

public class ServiceDriverInstantiator {

    public ServiceDriver getDriver(String serviceName) {
        String serviceClassName = PropertiesHolder.getInstance().getProperty(DriversConfigurationPropertyKeys.DRIVER_CLASS_NAME_KEY, serviceName);
        return (ServiceDriver) new ClassFactory().createInstance(serviceClassName);
    }
}
