package cloud.fogbow.fns.common.plugins.authorization;

import cloud.fogbow.fns.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.fns.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.common.models.FederationUser;

public class DefaultAuthorizationPlugin implements AuthorizationPlugin {

    public DefaultAuthorizationPlugin() {
    }

    @Override
    public boolean isAuthorized(FederationUser federationUserToken, String cloudName, String operation, String type)
            throws UnauthorizedRequestException, UnexpectedException {
        return true;
    }

    @Override
    public boolean isAuthorized(FederationUser federationUserToken, String operation, String type)
            throws UnauthorizedRequestException, UnexpectedException {
        return true;
    }
}
