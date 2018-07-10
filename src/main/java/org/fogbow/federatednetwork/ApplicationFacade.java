package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.constants.Operation;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.manager.core.exceptions.UnexpectedException;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.instances.InstanceType;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.Order;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;
import org.fogbowcloud.manager.core.plugins.behavior.authorization.AuthorizationPlugin;
import org.fogbowcloud.manager.core.plugins.behavior.federationidentity.FederationIdentityPlugin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ApplicationFacade {

    private static ApplicationFacade instance;

    private FederatedNetworkController federatedNetworkController;

    // TODO: Implement a singleton that loads a property for those plugins
    private FederationIdentityPlugin federationIdentityPlugin = new AllowAllIdentityPlugin();

    private AuthorizationPlugin authorizationPlugin = new AllowAllAuthorizationPlugin();

    public synchronized static ApplicationFacade getInstance() {
        if (instance == null) {
            instance = new ApplicationFacade();
        }
        return instance;
    }

    // federated network methods

    public String createFederatedNetwork(FederatedNetwork federatedNetwork, String federationTokenValue) throws
            UnauthenticatedUserException, UnexpectedException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO: Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.CREATE);

        return federatedNetworkController.create(federatedNetwork, federationUser);
    }

    public FederatedNetwork getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO: Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.GET);

        return federatedNetworkController.getFederatedNetwork(federatedNetworkId, federationUser);
    }

    public Collection<FederatedNetwork> getFederatedNetworks(String federationTokenValue) throws
            UnauthenticatedUserException, UnexpectedException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.GET);

        return federatedNetworkController.getUserFederatedNetworks(federationUser);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws NotEmptyFederatedNetworkException,
            FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {

        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.DELETE);

        federatedNetworkController.deleteFederatedNetwork(federatedNetworkId, federationUser);
    }

    // compute methods

    public ComputeOrder addFederatedAttributesIfApplied(FederatedComputeOrder federatedComputeOrder, String federationTokenValue)
            throws SubnetAddressesCapacityReachedException,
            IOException, FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {

        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.CREATE);

        ComputeOrder incrementedComputeOrder = federatedNetworkController.addFederatedAttributesIfApplied(federatedComputeOrder, federationUser);
        return incrementedComputeOrder;
    }

    public void updateOrderId(FederatedComputeOrder federatedCompute, String newId, String federationTokenValue)
            throws FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {
        FederationUser federationUser = getFederationUser(federationTokenValue);
        federatedCompute.setFederationUser(federationUser);
        federatedNetworkController.updateOrderId(federatedCompute, newId);
    }

    public ComputeInstance addFederatedAttributesIfApplied(ComputeInstance computeInstance, String federationTokenValue)
            throws FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        // TODO:  Check if we really want to use core authorization plugin.
        authorize(federationUser, Operation.GET);

        return federatedNetworkController.addFederatedInstanceAttributesIfApplied(computeInstance, federationUser, federationTokenValue);
    }

    public void deleteCompute(String computeId, String federationTokenValue) throws FederatedComputeNotFoundException,
            UnauthenticatedUserException, UnexpectedException {
        authenticate(federationTokenValue);
        FederationUser federationUser = getFederationUser(federationTokenValue);
        authorize(federationUser, Operation.DELETE);

        federatedNetworkController.deleteCompute(computeId, federationUser);
    }

    private void authenticate(String federationTokenValue) throws UnauthenticatedUserException {
        if (!this.federationIdentityPlugin.isValid(federationTokenValue)) {
            throw new UnauthenticatedUserException();
        }
    }

    private void authorize(FederationUser federationUser, Operation operation) throws UnauthenticatedUserException {
        if (!this.authorizationPlugin.isAuthorized(federationUser, operation)) {
            throw new UnauthenticatedUserException();
        }
    }

    private FederationUser getFederationUser(String federationTokenValue) throws UnauthenticatedUserException, UnexpectedException {
        return this.federationIdentityPlugin.getFederationUser(federationTokenValue);
    }

	/*
    public void deleteCompute(String computeOrderId, String federationTokenValue)
			throws  {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.DELETE);

		federateComputeUtil.deleteCompute(computeOrderId, federationUser);
	}*/

    public void setFederatedNetworkController(FederatedNetworkController federatedNetworkController) {
        this.federatedNetworkController = federatedNetworkController;
    }

    class AllowAllIdentityPlugin implements FederationIdentityPlugin {

        @Override
        public String createFederationTokenValue(Map<String, String> map) {
            return null;
        }

        @Override
        public FederationUser getFederationUser(String s) throws UnexpectedException {
            Map<String, String> attributes = new HashMap();

            attributes.put("user_name", "default_user");

            return new FederationUser("fed-user", attributes);
        }

        @Override
        public boolean isValid(String s) {
            return true;
        }

    }

    class AllowAllAuthorizationPlugin implements AuthorizationPlugin {

        @Override
        public boolean isAuthorized(FederationUser federationUser, Operation operation, Order order) {
            return true;
        }

        @Override
        public boolean isAuthorized(FederationUser federationUser, Operation operation, InstanceType instanceType) {
            return true;
        }

        @Override
        public boolean isAuthorized(FederationUser federationUser, Operation operation) {
            return true;
        }
    }

}
