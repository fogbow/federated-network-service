package org.fogbow.federatednetwork.controllers;

import org.apache.commons.io.IOUtils;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.plugins.cloud.compute.util.CloudInitUserDataBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FederateComputeUtil {

	// TODO: Place this in a property conf.
	private static final String IPSEC_INSTALLATION_PATH = "bin/ipsec-configuration";

	protected static final String LEFT_SOURCE_IP_KEY = "#LEFT_SOURCE_IP#";
	protected static final String RIGHT_IP = "#RIGHT_IP#";
	protected static final String RIGHT_SUBNET_KEY = "#RIGHT_SUBNET#";
	protected static final String IS_FEDERATED_VM_KEY = "#IS_FEDERATED_VM#";

	public static ComputeOrder addUserData(ComputeOrder computeOrder, String federatedComputeIp,
	                                   String agentPublicIp, String cidr)
			throws SubnetAddressesCapacityReachedException, IOException {
		InputStream inputStream = new FileInputStream(IPSEC_INSTALLATION_PATH);
		String cloudInitScript = IOUtils.toString(inputStream);
		String newScript = replaceScriptValues(cloudInitScript, federatedComputeIp, agentPublicIp, cidr);

		UserData userData = new UserData(newScript, CloudInitUserDataBuilder.FileType.SHELL_SCRIPT);
		ComputeOrder actualComputeOrder = createComputeWithUserData(computeOrder, userData);
		return actualComputeOrder;
	}

	/*public static void deleteCompute(String computeOrderId, FederationUser federationUser){
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

	private static ComputeOrder createComputeWithUserData(ComputeOrder computeOrder, UserData userData) {
		ComputeOrder newCompute = new ComputeOrder(computeOrder.getId(), computeOrder.getFederationUser(),
				computeOrder.getRequestingMember(), computeOrder.getProvidingMember(), computeOrder.getvCPU(),
				computeOrder.getMemory(), computeOrder.getDisk(), computeOrder.getImageId(),
				userData, computeOrder.getPublicKey());
		return newCompute;
	}

	private static String replaceScriptValues(String script, String federatedComputeIp, String agentPublicIp, String cidr) {
		String isFederatedVM = "true";
		script.replace(IS_FEDERATED_VM_KEY, isFederatedVM);
		script.replace(LEFT_SOURCE_IP_KEY, federatedComputeIp);
		script.replace(RIGHT_IP, agentPublicIp);
		script.replace(RIGHT_SUBNET_KEY, cidr);
		return script;
	}
}
