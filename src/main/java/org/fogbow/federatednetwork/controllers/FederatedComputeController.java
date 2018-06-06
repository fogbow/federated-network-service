package org.fogbow.federatednetwork.controllers;

import org.apache.commons.io.IOUtils;
import org.fogbow.federatednetwork.FederatedNetworksDB;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.token.FederationUser;
import org.fogbowcloud.manager.core.plugins.cloud.compute.util.CloudInitUserDataBuilder;

import java.io.*;
import java.util.Collection;
import java.util.Map;

public class FederatedComputeController {

	public static final String IPSEC_INSTALLATION_PATH = "bin/ipsec-installation";
	private FederatedNetworkController federatedNetworkController;

	private FederatedNetworksDB database;

	// TODO: Constructor missing

	public void activateCompute(ComputeOrder computeOrder, String federatedNetworkId, FederationUser federationUser)
			throws SubnetAddressesCapacityReachedException, IOException {
		if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
			FederatedNetwork federatedNetwork;
			federatedNetwork = federatedNetworkController.getFederatedNetwork(federatedNetworkId, federationUser);
			if (!federatedNetwork.isFull()) {
				String federatedIp = federatedNetwork.nextFreeIp(computeOrder.getId());
				InputStream inputStream = new FileInputStream(IPSEC_INSTALLATION_PATH);
				String script = IOUtils.toString(inputStream);
				UserData userData = new UserData(script, CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
				ComputeOrder actualComputeOrder = createOrderWithUserData(computeOrder, userData);
				// There's no setUserData in ComputeOrder
			}
		}
		// send to Core
	}

	public ComputeInstance getCompute(String computeOrderId, FederationUser federationUser){
		ComputeInstance computeInstance = null;
		// get compute from Core
		final Collection<FederatedNetwork> userNetworks = database.getUserNetworks(federationUser);
		if (!userNetworks.isEmpty()) {
			final String federatedIp = getFederatedIp(computeOrderId, federationUser);
			if (!federatedIp.isEmpty()) {
				// add federatedIp as a new attribute
			}
		}
		return computeInstance;
	}

	public void deleteCompute(String computeOrderId, FederationUser federationUser){
		final Collection<FederatedNetwork> userNetworks = database.getUserNetworks(federationUser);
		// get federatedNetwork related to compute's IP.
		final String federatedIp = getFederatedIp(computeOrderId, federationUser);

		if (!federatedIp.isEmpty()) {
			// release ip from network

		}
		throw new UnsupportedOperationException();
		// delete on Core
	}

	private String getFederatedIp(String computeOrderId, FederationUser federationUser){
		final Collection<FederatedNetwork> userNetworks = database.getUserNetworks(federationUser);
		String federatedIp = "";
		for (FederatedNetwork federatedNetwork: userNetworks){
			final Map<String, String> orderIpMap = federatedNetwork.getOrderIpMap();
			if (orderIpMap.containsKey(computeOrderId)) {
				federatedIp = orderIpMap.get(computeOrderId);
			}
		}
		return federatedIp;
	}

	private ComputeOrder createOrderWithUserData(ComputeOrder computeOrder, UserData userData) {
		ComputeOrder newComputeOrder = new ComputeOrder(computeOrder.getId(), computeOrder.getFederationUser(),
				computeOrder.getRequestingMember(), computeOrder.getProvidingMember(), computeOrder.getvCPU(),
				computeOrder.getMemory(), computeOrder.getDisk(), computeOrder.getImageId(),
				userData, computeOrder.getPublicKey());
		return newComputeOrder;
	}
}
