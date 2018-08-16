package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.manager.core.constants.Operation;
import org.fogbowcloud.manager.core.exceptions.InvalidParameterException;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.manager.core.models.InstanceStatus;
import org.fogbowcloud.manager.core.models.ResourceType;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;
import org.fogbowcloud.manager.core.plugins.behavior.authentication.AuthenticationPlugin;
import org.fogbowcloud.manager.core.plugins.behavior.authorization.AuthorizationPlugin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ApplicationFacade {

    private static ApplicationFacade instance;

    private OrderController orderController;

    // TODO: Implement a singleton that loads a property for those plugins
    private AuthenticationPlugin federationIdentityPlugin = new AllowAllIdentityPlugin();

    private AuthorizationPlugin authorizationPlugin = new AllowAllAuthorizationPlugin();

    public synchronized static ApplicationFacade getInstance() {
        if (instance == null) {
            instance = new ApplicationFacade();
        }
        return instance;
    }

    // federated network methods

    public String createFederatedNetwork(FederatedNetworkOrder federatedNetwork, String federationTokenValue) throws
            UnauthenticatedUserException, InvalidParameterException, InvalidCidrException, AgentCommucationException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO: Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.CREATE);

        return orderController.activateFederatedNetwork(federatedNetwork, federationUser);
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws FederatedComputeNotFoundException, UnauthenticatedUserException, InvalidParameterException, FederatedNetworkNotFoundException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO: Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.GET);

        return orderController.getFederatedNetwork(federatedNetworkId, federationUser);
    }

    public Collection<InstanceStatus> getFederatedNetworksStatus(String federationTokenValue) throws
            UnauthenticatedUserException, InvalidParameterException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.GET);

        return orderController.getUserFederatedNetworksStatus(federationUser);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws NotEmptyFederatedNetworkException,
            FederatedComputeNotFoundException, UnauthenticatedUserException, InvalidParameterException, FederatedNetworkNotFoundException, AgentCommucationException {

        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.DELETE);

        orderController.deleteFederatedNetwork(federatedNetworkId, federationUser);
    }

    // compute methods

    public ComputeOrder addFederatedIpInPostIfApplied(FederatedComputeOrder federatedComputeOrderOld, String federationTokenValue)
            throws SubnetAddressesCapacityReachedException, IOException, UnauthenticatedUserException,
            InvalidParameterException, FederatedComputeNotFoundException, FederatedNetworkNotFoundException, InvalidCidrException {

        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.CREATE);

        ComputeOrder incrementedComputeOrder = orderController.addFederationUserDataIfApplied(federatedComputeOrderOld, federationUser);
        return incrementedComputeOrder;
    }

    public void updateOrderId(FederatedComputeOrder federatedCompute, String newId, String federationTokenValue)
            throws FederatedComputeNotFoundException, UnauthenticatedUserException,  InvalidParameterException {
        FederationUser federationUser = getFederationUser(federationTokenValue);
        federatedCompute.getComputeOrder().setFederationUser(federationUser);
        orderController.updateIdOnComputeCreation(federatedCompute, newId);
    }

    public ComputeInstance addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance, String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.GET);

        return orderController.addFederatedIpInGetInstanceIfApplied(computeInstance, federationUser);
    }

    public void deleteCompute(String computeId, String federationTokenValue) throws FederatedComputeNotFoundException,
            UnauthenticatedUserException, InvalidParameterException, FederatedNetworkNotFoundException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        authorize(federationUser, Operation.DELETE);

        orderController.deleteCompute(computeId);
    }

    public void rollbackInFailedPost(FederatedComputeOrder federatedCompute) {
        orderController.rollbackInFailedPost(federatedCompute);
    }

    private void authenticate(String federationTokenValue) throws UnauthenticatedUserException {
        if (!this.federationIdentityPlugin.isValid(federationTokenValue)) {
            throw new UnauthenticatedUserException();
        }
    }

    private void authorize(FederationUser federationUser, Operation operation) throws UnauthenticatedUserException {
        // FIXME: this resource type will be changed, we cannot implement federated network type in resource-allocation-service
        if (!this.authorizationPlugin.isAuthorized(federationUser, operation, ResourceType.NETWORK)) {
            throw new UnauthenticatedUserException();
        }
    }

    private FederationUser getFederationUser(String federationTokenValue) throws UnauthenticatedUserException, InvalidParameterException {
        return this.federationIdentityPlugin.getFederationUser(federationTokenValue);
    }

    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    class AllowAllIdentityPlugin implements AuthenticationPlugin {

        @Override
        public FederationUser getFederationUser(String s) throws InvalidParameterException {
            Map<String, String> attributes = new HashMap();

            attributes.put(FederationUser.MANDATORY_NAME_ATTRIBUTE, "default_user");

            return new FederationUser("fed-user", attributes);
        }

        @Override
        public boolean isValid(String s) {
            return true;
        }

    }

    class AllowAllAuthorizationPlugin implements AuthorizationPlugin {
        @Override
        public boolean isAuthorized(FederationUser federationUser, Operation operation, ResourceType resourceType) {
            return true;
        }
    }

}
