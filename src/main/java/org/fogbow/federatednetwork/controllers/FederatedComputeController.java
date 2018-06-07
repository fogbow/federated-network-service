package org.fogbow.federatednetwork.controllers;

import org.apache.commons.io.IOUtils;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.token.FederationUser;
import org.fogbowcloud.manager.core.plugins.cloud.compute.util.CloudInitUserDataBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FederatedComputeController {

	private static final String IPSEC_INSTALLATION_PATH = "bin/ipsec-installation";

	// TODO: Constructor missing

	public static void activateCompute(ComputeOrder computeOrder, String federatedNetworkId, FederationUser federationUser)
			throws SubnetAddressesCapacityReachedException, IOException {
		InputStream inputStream = new FileInputStream(IPSEC_INSTALLATION_PATH);
		String script = IOUtils.toString(inputStream);
		UserData userData = new UserData(script, CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
		// TODO: Change UserData to set required parameters, ex: federatedIp
//		ComputeOrder actualComputeOrder = createOrderWithUserData(computeOrder, userData);
	}

	/*public static FederatedComputeInstance getCompute(ComputeInstance computeInstance){
		// get compute from Core
		final Collection<FederatedNetwork> userNetworks = database.getUserFederatedNetworks(federationUser);
		if (!userNetworks.isEmpty()) {
			final String federatedIp = getFederatedIp(computeOrderId, federationUser);
			if (!federatedIp.isEmpty()) {
				// add federatedIp as a new attribute
			}
		}
		return computeInstance;
	}

	public static void deleteCompute(String computeOrderId, FederationUser federationUser){
		final Collection<FederatedNetwork> userNetworks = database.getUserFederatedNetworks(federationUser);
		// get federatedNetwork related to compute's IP.
		final String federatedIp = getFederatedIp(computeOrderId, federationUser);

		if (!federatedIp.isEmpty()) {
			// release ip from network

		}
		throw new UnsupportedOperationException();
		// delete on Core
	}

	public static String getFederatedIp(String computeOrderId, FederationUser federationUser){
		final Collection<FederatedNetwork> userNetworks = database.getUserFederatedNetworks(federationUser);
		String federatedIp = "";
		for (FederatedNetwork federatedNetwork: userNetworks){
			final Map<String, String> orderIpMap = federatedNetwork.getComputeIpMap();
			if (orderIpMap.containsKey(computeOrderId)) {
				federatedIp = orderIpMap.get(computeOrderId);
			}
		}
		return federatedIp;
	}*/

	private ComputeOrder createOrderWithUserData(ComputeOrder computeOrder, UserData userData) {
		ComputeOrder newComputeOrder = new ComputeOrder(computeOrder.getId(), computeOrder.getFederationUser(),
				computeOrder.getRequestingMember(), computeOrder.getProvidingMember(), computeOrder.getvCPU(),
				computeOrder.getMemory(), computeOrder.getDisk(), computeOrder.getImageId(),
				userData, computeOrder.getPublicKey());
		return newComputeOrder;
	}
}
