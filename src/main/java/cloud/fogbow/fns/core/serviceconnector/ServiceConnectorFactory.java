package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.model.ConfigurationMode;

public class ServiceConnectorFactory {
    // TODO ARNETT
    private static final String LOCAL_MEMBER_NAME = "fakeMemberName";

    private static ServiceConnectorFactory instance = null;

    private ServiceConnectorFactory() {
    }

    public static ServiceConnectorFactory getInstance() {
        if (instance == null) {
            instance = new ServiceConnectorFactory();
        }
        return instance;
    }

    public ServiceConnector getServiceConnector(ConfigurationMode configurationMode, String memberName) {
        switch (configurationMode) {
            case DFNS:
                if (memberName.equals(LOCAL_MEMBER_NAME)) {
                    return new LocalDfnsServiceConnector();
                } else {
                    return new RemoteDfnsServiceConnector();
                }
            case VANILLA:
                return new VanillaServiceConnector();
            default:
                throw new RuntimeException(Messages.Exception.CONFIGURATION_MODE_NOT_IMPLEMENTED);
        }
    }
}
