package org.fogbow.federatednetwork.common.plugins.authorization;

import org.fogbow.federatednetwork.common.exceptions.UnauthorizedRequestException;
import org.fogbow.federatednetwork.common.exceptions.UnexpectedException;
import org.fogbow.federatednetwork.common.models.FederationUser;

public interface AuthorizationPlugin {
    /**
     * Verifies if the user described by federationToken is authorized to perform the operation on the
     * type of resource indicated, in the cloud indicated.
     *
     *
     * @param federationUser the FederationUser object describing the user to be authorized
     * @param cloudName the name of the cloud to which the request has been sent
     * @param operation the name of the operation the user is requesting to perform
     * @param target the name of the resource or resource type on which the operation will be executed
     * @return a boolean stating whether the user is authorized or not.
     */
    public boolean isAuthorized(FederationUser federationUser, String cloudName, String operation, String target)
            throws UnauthorizedRequestException, UnexpectedException;

    /**
     * Verifies if the user described by federationToken is authorized to perform the operation on the
     * type of resource indicated, in the default cloud.
     *
     *
     * @param federationUser the FederationUser object describing the user to be authorized
     * @param operation the name of the operation the user is requesting to perform
     * @param target the name of the resource or resource type on which the operation will be executed
     * @return a boolean stating whether the user is authorized or not.
     */
    public boolean isAuthorized(FederationUser federationUser, String operation, String target)
            throws UnauthorizedRequestException, UnexpectedException;
}
