package org.fogbow.federatednetwork.controllers;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.FederatedNetworkConstants;
import org.fogbow.federatednetwork.FederatedNetworksDB;
import org.fogbow.federatednetwork.ProcessUtil;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeInstance;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.token.FederationUser;

import java.io.IOException;
import java.util.*;

public class FederatedNetworkController {

	private static final Logger LOGGER = Logger.getLogger(FederatedNetworkController.class);

	private static final String DATABASE_FILE_PATH = "federated-networks.db";

	private String permissionFilePath;
	private String agentUser;
	private String agentPublicIp;
	private String agentPrivateIp;

	private FederatedNetworksDB database;

	public FederatedNetworkController(String permissionFilePath, String agentUser, String agentPrivateIp, String agentPublicIp) {
		this(permissionFilePath, agentUser, agentPrivateIp, agentPublicIp, DATABASE_FILE_PATH);
	}

	public FederatedNetworkController(String permissionFilePath, String agentUser, String agentPrivateIp,
	                                  String agentPublicIp, String databaseFilePath) {
		this.permissionFilePath = permissionFilePath;
		this.agentUser = agentUser;
		this.agentPrivateIp = agentPrivateIp;
		this.agentPublicIp = agentPublicIp;

		this.database = new FederatedNetworksDB(databaseFilePath);
	}

	public String create(FederatedNetwork federatedNetwork, FederationUser user) {
		initFederatedNetwork(federatedNetwork);

		SubnetUtils.SubnetInfo subnetInfo = getSubnetInfo(federatedNetwork.getCidrNotation());

		if (!isValid(subnetInfo)) {
			LOGGER.error("Subnet (" + federatedNetwork.getCidrNotation() + ") invalid");
			return null;
		}

		boolean createdSuccessfully = addFederatedNetworkOnAgent(federatedNetwork.getCidrNotation(), subnetInfo.getLowAddress());
		if (createdSuccessfully) {
			if (database.addFederatedNetwork(federatedNetwork, user)) {
				return federatedNetwork.getId();
			} else {
				return null;
			}
		}

		return null;
	}

	private void initFederatedNetwork(FederatedNetwork federatedNetwork) {
		federatedNetwork.setId(String.valueOf(UUID.randomUUID()));
		federatedNetwork.setIpsServed(1);
		federatedNetwork.setFreedIps(new LinkedList<>());
		federatedNetwork.setComputeIpMap(new HashMap<>());
	}

	public boolean addFederatedNetworkOnAgent(String cidrNotation, String virtualIpAddress) {
		ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
				"StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
				"sudo", "/home/ubuntu/config-ipsec", agentPrivateIp, agentPublicIp, cidrNotation, virtualIpAddress);
		LOGGER.info("Trying to call agent with atts (" + cidrNotation + "): " + builder.command());

		int resultCode = 0;
		try {
			Process process = builder.start();
			LOGGER.info("Trying agent with atts (" + cidrNotation + "). Output : " + ProcessUtil.getOutput(process));
			LOGGER.info("Trying agent with atts (" + cidrNotation + "). Error : " + ProcessUtil.getError(process));
			resultCode = process.waitFor();
			if (resultCode == 0) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		LOGGER.error("Is not possible call agent. Process command: " + resultCode);
		return false;
	}

	public Collection<FederatedNetwork> getUserFederatedNetworks(FederationUser user) {
		return database.getUserNetworks(user);
	}

	public FederatedNetwork getFederatedNetwork(String federatedNetworkId, FederationUser user) throws FederatedComputeNotFoundException {
		Collection<FederatedNetwork> allFederatedNetworks = this.getUserFederatedNetworks(user);
		for (FederatedNetwork federatedNetwork : allFederatedNetworks) {
			if (federatedNetwork.getId().equals(federatedNetworkId)) {
				return federatedNetwork;
			}
		}

		throw new FederatedComputeNotFoundException();
	}

	public void updateFederatedNetworkMembers(FederationUser user, String federatedNetworkId,
	                                          Set<String> membersSet) throws FederatedComputeNotFoundException {
		FederatedNetwork federatedNetwork = getFederatedNetwork(federatedNetworkId, user);
		if (federatedNetwork == null) {
			throw new IllegalArgumentException(
					FederatedNetworkConstants.NOT_FOUND_FEDERATED_NETWORK_MESSAGE
							+ federatedNetworkId);
		}
		for (String member : membersSet) {
			federatedNetwork.addFederationNetworkMember(member);
		}

		if (!this.database.addFederatedNetwork(federatedNetwork, user)) {
			throw new IllegalArgumentException(
					FederatedNetworkConstants.CANNOT_UPDATE_FEDERATED_NETWORK_IN_DATABASE
							+ federatedNetworkId);
		}
	}

	public String getCIDRFromFederatedNetwork(String federatedNetworkId, FederationUser user) throws FederatedComputeNotFoundException {
		FederatedNetwork federatedNetwork = getFederatedNetwork(federatedNetworkId, user);
		if (federatedNetwork == null) {
			throw new IllegalArgumentException(
					FederatedNetworkConstants.NOT_FOUND_FEDERATED_NETWORK_MESSAGE
							+ federatedNetworkId);
		}
		return federatedNetwork.getCidrNotation();
	}

	public String getPrivateIpFromFederatedNetwork(String federatedNetworkId, String orderId, FederationUser user) throws SubnetAddressesCapacityReachedException, FederatedComputeNotFoundException {
		LOGGER.info("Getting FN Ip to Order: " + orderId);
		FederatedNetwork federatedNetwork = this.getFederatedNetwork(federatedNetworkId, user);
		String privateIp = federatedNetwork.nextFreeIp(orderId);
		LOGGER.info("FederatedNetwork: " + federatedNetwork.toString());
		if (!this.database.addFederatedNetwork(federatedNetwork, user)) {
			throw new IllegalArgumentException(
					FederatedNetworkConstants.CANNOT_UPDATE_FEDERATED_NETWORK_IN_DATABASE);
		}
		return privateIp;
	}

	public void deleteFederatedNetwork(String federatedNetworkId, FederationUser user) throws
			NotEmptyFederatedNetworkException, FederatedComputeNotFoundException {
		LOGGER.info("Initializing delete method, user: " + user + ", federated network id: " + federatedNetworkId);
		FederatedNetwork federatedNetwork = this.getFederatedNetwork(federatedNetworkId, user);
		if (federatedNetwork == null) {
			throw new IllegalArgumentException(
					FederatedNetworkConstants.NOT_FOUND_FEDERATED_NETWORK_MESSAGE
							+ federatedNetworkId);
		}
		LOGGER.info("Trying to delete federated network: " + federatedNetwork.toString());
		// TODO: check if deleteFederatedNetworkFromAgent is false (whether don't find script file, for example).
		if (!federatedNetwork.getComputeIpMap().isEmpty()) {
			throw new NotEmptyFederatedNetworkException();
		}
		if (deleteFederatedNetworkFromAgent(federatedNetwork.getCidrNotation()) == true) {
			LOGGER.info("Successfully deleted federated network: " + federatedNetwork.toString() + " on agent.");
			if (!this.database.delete(federatedNetwork, user)) {
				LOGGER.info("Error to delete federated network: " + federatedNetwork.toString());
				throw new IllegalArgumentException(
						FederatedNetworkConstants.CANNOT_UPDATE_FEDERATED_NETWORK_IN_DATABASE
								+ federatedNetworkId);
			}
		}
	}

	public boolean deleteFederatedNetworkFromAgent(String cidrNotation) {
		ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
				"StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
				"sudo", "/home/ubuntu/remove-network", cidrNotation);
		LOGGER.info("Trying to remove network on agent with atts (" + cidrNotation + "): " + builder.command());

		int resultCode = 0;
		try {
			Process process = builder.start();
			LOGGER.info("Trying remove network on agent with CIDR (" + cidrNotation + "). Output : " + ProcessUtil.getOutput(process));
			LOGGER.info("Trying remove network on agent with CIDR (" + cidrNotation + "). Error : " + ProcessUtil.getError(process));
			resultCode = process.waitFor();
			if (resultCode == 0) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		LOGGER.error("Is not possible remove network on agent. Process command: " + resultCode);
		return false;
	}

	public ComputeOrder addFederatedAttributesIfApplied(ComputeOrder computeOrder, String federatedNetworkId, FederationUser federationUser)
			throws SubnetAddressesCapacityReachedException, IOException, FederatedComputeNotFoundException {

		if (federatedNetworkId != null && !federatedNetworkId.isEmpty()) {
			FederatedNetwork federatedNetwork;
			federatedNetwork = getFederatedNetwork(federatedNetworkId, federationUser);
			String federatedIp = getPrivateIpFromFederatedNetwork(federatedNetworkId, computeOrder.getId(), federationUser);
			return FederateComputeUtil.addUserData(computeOrder, federatedIp, agentPublicIp, federatedNetwork.getCidrNotation());
		}

		return computeOrder;
	}

	public ComputeInstance addFederatedInstanceAttributesIfApplied(ComputeInstance computeInstance,
	                                                               FederationUser federationUser, String federationTokenValue)
			throws FederatedComputeNotFoundException {

		Collection<FederatedNetwork> userNetworks = database.getUserNetworks(federationUser);
		if (!userNetworks.isEmpty()) {
			String federatedIp = getAssociatedFederatedIp(computeInstance.getId(), federationUser);
			if (federatedIp != null) {
				return new FederatedComputeInstance(computeInstance, federatedIp);
			}
		}

		return computeInstance;
	}

	public void deleteCompute(String computeId, FederationUser federationUser, String federationTokenValue) throws FederatedComputeNotFoundException {
		String federatedIp = getAssociatedFederatedIp(computeId, federationUser);
		final Collection<FederatedNetwork> userNetworks = database.getUserNetworks(federationUser);
		for (FederatedNetwork federatedNetwork : userNetworks) {
			Map<String, String> orderIpMap = federatedNetwork.getComputeIpMap();
			if (orderIpMap.containsKey(computeId)) {
				federatedNetwork.freeIp(federatedIp, computeId);
			}
		}
	}

	private String getAssociatedFederatedIp(String computeOrderId, FederationUser federationUser) throws FederatedComputeNotFoundException {
		Collection<FederatedNetwork> userNetworks = database.getUserNetworks(federationUser);
		for (FederatedNetwork federatedNetwork: userNetworks){
			Map<String, String> orderIpMap = federatedNetwork.getComputeIpMap();
			if (orderIpMap.containsKey(computeOrderId)) {
				return orderIpMap.get(computeOrderId);
			}
		}
		return null;
	}

	private static SubnetUtils.SubnetInfo getSubnetInfo(String cidrNotation) {
		return new SubnetUtils(cidrNotation).getInfo();
	}

	private static boolean isValid(SubnetUtils.SubnetInfo subnetInfo) {
		int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
		int highAddress = subnetInfo.asInteger(subnetInfo.getHighAddress());
		return highAddress - lowAddress > 1;
	}

}
