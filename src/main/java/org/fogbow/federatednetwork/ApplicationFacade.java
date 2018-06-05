package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederatedComputeController;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.AaController;
import org.fogbowcloud.manager.core.constants.Operation;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.token.FederationUser;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;

public class ApplicationFacade {

	private FederatedNetworkController federatedNetworksController;
	private FederatedComputeController federatedComputeController;
	private AaController aaController;

	// TODO: Change methods return type

	public void createFederatedNetwork(FederatedNetwork federatedNetwork, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.CREATE);

		federatedNetworksController.create(federatedNetwork, federationUser);
	}

	public void getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.GET);

		federatedNetworksController.getFederatedNetwork(federatedNetworkId, federationUser);
	}

	public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.DELETE);

		federatedNetworksController.deleteFederatedNetwork(federatedNetworkId, federationUser);
	}

	public void createCompute(ComputeOrder computeOrder, String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.DELETE);

		federatedComputeController.activateCompute(computeOrder, federatedNetworkId);
	}

	public void getCompute(String computeOrderId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.GET);

		federatedComputeController.getCompute(computeOrderId);
	}

	public void deleteCompute(String computeOrderId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.GET);

		federatedComputeController.deleteCompute(computeOrderId);
	}
}
