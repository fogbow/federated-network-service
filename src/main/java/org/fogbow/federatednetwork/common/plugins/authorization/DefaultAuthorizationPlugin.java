package org.fogbow.federatednetwork.common.plugins.authorization;

import org.fogbow.federatednetwork.common.exceptions.UnauthorizedRequestException;
import org.fogbow.federatednetwork.common.exceptions.UnexpectedException;
import org.fogbow.federatednetwork.common.models.FederationUser;

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
