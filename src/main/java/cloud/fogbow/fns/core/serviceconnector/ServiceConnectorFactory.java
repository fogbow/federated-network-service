package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.model.ConfigurationMode;

public class ServiceConnectorFactory {
    private static ServiceConnectorFactory instance = null;

    private ServiceConnectorFactory() {
    }

    public static ServiceConnectorFactory getInstance() {
        if (instance == null) {
            instance = new ServiceConnectorFactory();
        }
        return instance;
    }

    public ServiceConnector getServiceConnector(ConfigurationMode configurationMode) {
        switch (configurationMode) {
            case DFNS:
                return new DfnsServiceConnector();
            case VANILLA:
                return new VanillaServiceConnector();
            default:
                throw new RuntimeException(Messages.Exception.CONFIGURATION_MODE_NOT_IMPLEMENTED);
        }
    }
}
