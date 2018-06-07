package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederatedComputeController;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.AaController;
import org.fogbowcloud.manager.core.constants.Operation;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.token.FederationUser;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;

import java.io.IOException;

public class ApplicationFacade {

	private static ApplicationFacade instance;

	private FederatedNetworkController federatedNetworkController;
	private FederatedComputeController federatedComputeController;
	private AaController aaController;

	public static ApplicationFacade getInstance() {
		synchronized (ApplicationFacade.class) {
			if (instance == null) {
				instance = new ApplicationFacade();
			}
			return instance;
		}
	}

	// TODO: Change methods return type

	public void createFederatedNetwork(FederatedNetwork federatedNetwork, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.CREATE);

		federatedNetworkController.create(federatedNetwork, federationUser);
	}

	public void getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.GET);

		federatedNetworkController.getFederatedNetwork(federatedNetworkId, federationUser);
	}

	public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.DELETE);

		federatedNetworkController.deleteFederatedNetwork(federatedNetworkId, federationUser);
	}

	public void createCompute(ComputeOrder computeOrder, String federatedNetworkId, String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException, SubnetAddressesCapacityReachedException, IOException {
		this.aaController.authenticate(federationTokenValue);
		FederationUser federationUser = this.aaController.getFederationUser(federationTokenValue);
		// TODO:  Check if we really want to use core authorization plugin.
		this.aaController.authorize(federationUser, Operation.DELETE);

		federatedComputeController.activateCompute(computeOrder, federatedNetworkId, federationUser);
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

	public void setAaController(AaController aaController) {
		this.aaController = aaController;
	}

	public void setFederatedNetworkController(FederatedNetworkController federatedNetworkController) {
		this.federatedNetworkController = federatedNetworkController;
	}

	public void setFederatedComputeController(FederatedComputeController federatedComputeController) {
		this.federatedComputeController = federatedComputeController;
	}
}
