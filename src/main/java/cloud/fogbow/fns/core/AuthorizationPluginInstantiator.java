package cloud.fogbow.fns.core;

import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.fns.core.model.FnsOperation;

public class AuthorizationPluginInstantiator {
    private static ClassFactory classFactory = new ClassFactory();

    public static AuthorizationPlugin<FnsOperation> getAuthorizationPlugin(String className) {
        return (AuthorizationPlugin<FnsOperation>) AuthorizationPluginInstantiator.classFactory.createInstance(className);
    }
}
