package org.fogbow.federatednetwork.common.plugins.authorization;

import org.fogbow.federatednetwork.common.exceptions.UnauthorizedRequestException;
import org.fogbow.federatednetwork.common.exceptions.UnexpectedException;
import org.fogbow.federatednetwork.common.models.FederationUser;

public class AuthorizationController {
    private AuthorizationPlugin authorizationPlugin;

    public AuthorizationController(AuthorizationPlugin authorizationPlugin) {
        this.authorizationPlugin = authorizationPlugin;
    }

    public void authorize(FederationUser federationUser, String operation, String resourceType)
            throws UnexpectedException, UnauthorizedRequestException {
        this.authorizationPlugin.isAuthorized(federationUser, operation, resourceType);
    }
}
