package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederatedComputeController;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.constants.Operation;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.models.instances.InstanceType;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.Order;
import org.fogbowcloud.manager.core.models.token.FederationUser;
import org.fogbowcloud.manager.core.plugins.behavior.authorization.AuthorizationPlugin;
import org.fogbowcloud.manager.core.plugins.behavior.federationidentity.FederationIdentityPlugin;
import org.fogbowcloud.manager.core.plugins.exceptions.TokenValueCreationException;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ApplicationFacade {

	private static ApplicationFacade instance;

	private FederatedNetworkController federatedNetworkController;
	private FederatedComputeController federatedComputeController;

	// TODO: Implement a singleton that loads a property for those plugins
	private FederationIdentityPlugin federationIdentityPlugin = new FederationIdentityPlugin() {

		@Override
		public String createFederationTokenValue(Map<String, String> map) throws UnauthenticatedException, TokenValueCreationException, TokenValueCreationException {
			return null;
		}

		@Override
		public FederationUser getFederationUser(String s) throws UnauthenticatedException {
			Map<String, String> attributes = new HashMap();

			attributes.put("user_name", "default_user");

			return new FederationUser(0L, attributes);
		}

		@Override
		public boolean isValid(String s) {
			return true;
		}
	};

	private AuthorizationPlugin authorizationPlugin = new AuthorizationPlugin() {

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
	};

	public synchronized static ApplicationFacade getInstance() {
		if (instance == null) {
			instance = new ApplicationFacade();
		}
		return instance;
	}

	// TODO: Change methods return type

	public String createFederatedNetwork(FederatedNetwork federatedNetwork, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		authenticate(federationTokenValue);
		FederationUser federationUser = getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		authorize(federationUser, Operation.CREATE);

		return federatedNetworkController.create(federatedNetwork, federationUser);
	}

	public FederatedNetwork getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		authenticate(federationTokenValue);
		FederationUser federationUser = getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		authorize(federationUser, Operation.GET);

		return federatedNetworkController.getFederatedNetwork(federatedNetworkId, federationUser);
	}

	public Collection<FederatedNetwork> getFederatedNetworks(String federationTokenValue) throws UnauthenticatedException, UnauthorizedException {
		authenticate(federationTokenValue);
		FederationUser federationUser = getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		authorize(federationUser, Operation.GET);

		return federatedNetworkController.getUserFederatedNetworks(federationUser);
	}

	public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException, NotEmptyFederatedNetworkException {
		authenticate(federationTokenValue);
		FederationUser federationUser = getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		authorize(federationUser, Operation.DELETE);

		federatedNetworkController.deleteFederatedNetwork(federatedNetworkId, federationUser);
	}

	public void createCompute(ComputeOrder computeOrder, String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException, SubnetAddressesCapacityReachedException, IOException {
		authenticate(federationTokenValue);
		FederationUser federationUser = getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		authorize(federationUser, Operation.CREATE);

		federatedComputeController.activateCompute(computeOrder, federatedNetworkId, federationUser);
	}

	private void authenticate(String federationTokenValue) throws UnauthenticatedException {
		if (!this.federationIdentityPlugin.isValid(federationTokenValue)) {
			throw new UnauthenticatedException();
		}
	}

	private void authorize(FederationUser federationUser, Operation operation) throws UnauthorizedException {
		if (!this.authorizationPlugin.isAuthorized(federationUser, operation)) {
			throw new UnauthorizedException();
		}
	}

	private FederationUser getFederationUser(String federationTokenValue) throws UnauthenticatedException, UnauthorizedException {
		return this.federationIdentityPlugin.getFederationUser(federationTokenValue);
	}

	/*public void getCompute(String computeOrderId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.GET);

		federatedComputeController.getCompute(computeOrderId, federationUser);
	}

	public void deleteCompute(String computeOrderId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.DELETE);

		federatedComputeController.deleteCompute(computeOrderId, federationUser);
	}*/

	public void setFederatedNetworkController(FederatedNetworkController federatedNetworkController) {
		this.federatedNetworkController = federatedNetworkController;
	}

	public void setFederatedComputeController(FederatedComputeController federatedComputeController) {
		this.federatedComputeController = federatedComputeController;
	}
}
