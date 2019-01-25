package cloud.fogbow.fns.common.plugins.authorization;

import cloud.fogbow.fns.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.fns.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.common.models.FederationUser;

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
