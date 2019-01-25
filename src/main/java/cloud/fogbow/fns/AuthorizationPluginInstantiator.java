package cloud.fogbow.fns;

import cloud.fogbow.fns.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.fns.common.util.PluginFactory;
import cloud.fogbow.fns.constants.ConfigurationConstants;

public class AuthorizationPluginInstantiator {
    private static PluginFactory pluginFactory = new PluginFactory();

    public static AuthorizationPlugin getAuthorizationPlugin() {
        String className = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AUTHORIZATION_PLUGIN_CLASS);
        return (AuthorizationPlugin) AuthorizationPluginInstantiator.pluginFactory.createPluginInstance(className);
    }
}
