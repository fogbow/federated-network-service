package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.utils.BashScriptRunner;

public class ServiceConnectorFactory {
    private static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyDefaults.XMPP_JID_KEY);

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
