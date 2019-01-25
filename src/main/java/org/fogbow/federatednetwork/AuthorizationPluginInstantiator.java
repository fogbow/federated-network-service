package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.common.plugins.authorization.AuthorizationPlugin;
import org.fogbow.federatednetwork.common.util.PluginFactory;
import org.fogbow.federatednetwork.constants.ConfigurationConstants;

public class AuthorizationPluginInstantiator {
    private static PluginFactory pluginFactory = new PluginFactory();

    public static AuthorizationPlugin getAuthorizationPlugin() {
        String className = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AUTHORIZATION_PLUGIN_CLASS);
        return (AuthorizationPlugin) AuthorizationPluginInstantiator.pluginFactory.createPluginInstance(className);
    }
}
