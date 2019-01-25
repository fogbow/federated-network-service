package cloud.fogbow.fns;

import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.common.util.PluginFactory;
import cloud.fogbow.fns.constants.ConfigurationConstants;

public class AuthorizationPluginInstantiator {
    private static PluginFactory pluginFactory = new PluginFactory();

    public static AuthorizationPlugin getAuthorizationPlugin() {
        String className = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AUTHORIZATION_PLUGIN_CLASS);
        return (AuthorizationPlugin) AuthorizationPluginInstantiator.pluginFactory.createPluginInstance(className);
    }
}
