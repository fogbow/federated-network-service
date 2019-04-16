package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.utils.BashScriptRunner;

public class ServiceConnectorFactory {
    // TODO DFNS retrieve from file
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
                    return new LocalDfnsServiceConnector(new BashScriptRunner());
                } else {
                    return new RemoteDfnsServiceConnector(memberName);
                }
            case VANILLA:
                return new VanillaServiceConnector();
            default:
                throw new RuntimeException(Messages.Exception.CONFIGURATION_MODE_NOT_IMPLEMENTED);
        }
    }
}
