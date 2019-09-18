package cloud.fogbow.fns.core.drivers;

import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.vanilla.VanillaServiceDriver;
import cloud.fogbow.fns.core.model.ConfigurationMode;

public class ServiceDriverFactory {
    private static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.LOCAL_MEMBER_NAME_KEY);

    private static ServiceDriverFactory instance = null;

    private ServiceDriverFactory() {
    }

    public static ServiceDriverFactory getInstance() {
        if (instance == null) {
            instance = new ServiceDriverFactory();
        }
        return instance;
    }

    public ServiceDriver getServiceDriver(ConfigurationMode configurationMode) {
        switch (configurationMode) {
            case DFNS:
                return new DfnsServiceDriver();
            case VANILLA:
                return new VanillaServiceDriver();
            default:
                throw new RuntimeException(Messages.Exception.CONFIGURATION_MODE_NOT_IMPLEMENTED);
        }
    }
}
