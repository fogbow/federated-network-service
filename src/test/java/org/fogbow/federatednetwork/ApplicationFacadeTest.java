package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederateComputeUtil;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeInstance;
import org.fogbow.federatednetwork.model.FederatedComputeOrderOld;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.exceptions.InvalidParameterException;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.instances.InstanceState;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class ApplicationFacadeTest {

	private static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";

	private FederatedNetworkController federatedNetworkController;

	private String fakeId = "fakeId";

	@Before
	public void setUp(){
		deleteTestFiles();

		String permissionFilePath = "fake-file.pem";
		String agentUser = "fake-user";
		String agentPrivateIp = "fake-private-ip";
		String agentPublicIp = "fake-public-ip";
		String agentPSK = "fake-psk";
		String agentAddFedNetScriptPath = "fake-script-path";
		String agentRemoveFedNetScriptPath = "fake-script-path";

		federatedNetworkController = spy(new FederatedNetworkController(
				permissionFilePath, agentUser, agentPrivateIp, agentPublicIp, agentPSK, agentAddFedNetScriptPath,
				agentRemoveFedNetScriptPath, TEST_DATABASE_FILE_PATH));

		ApplicationFacade.getInstance().setFederatedNetworkController(federatedNetworkController);
	}

	@After
	public void clean() {
		deleteTestFiles();
	}

	private boolean deleteTestFiles() {
		return new File(TEST_DATABASE_FILE_PATH).delete();
	}

	/*@Test
	public void testFederatedNetwork() throws NotEmptyFederatedNetworkException, FederatedComputeNotFoundException,
			UnauthenticatedUserException, InvalidParameterException {
		String cidrNotation = "10.0.0.0/24";
		String label = "testNetwork";
		String fakeToken = "fake-token";
		Set<String> allowedMembers = new HashSet<>(Arrays.asList(new String[] {"member1", "member2"}));
		FederatedNetwork federatedNetwork = new FederatedNetwork(cidrNotation, label, allowedMembers, InstanceState.READY);

		Collection<FederatedNetwork> federatedNetworks = ApplicationFacade.getInstance().getFederatedNetworks(fakeToken);
		assertEquals(0, federatedNetworks.size());

		Mockito.doReturn(true).when(federatedNetworkController).addFederatedNetworkOnAgent(anyString(), anyString());

		String createdFederatedNetworkId = ApplicationFacade.getInstance().createFederatedNetwork(federatedNetwork, fakeToken);

		federatedNetworks = ApplicationFacade.getInstance().getFederatedNetworks(fakeToken);
		assertEquals(1, federatedNetworks.size());

		FederatedNetwork federatedNetworkById = ApplicationFacade.getInstance().getFederatedNetwork(
				createdFederatedNetworkId, fakeToken);

		assertEquals(createdFederatedNetworkId, federatedNetworkById.getId());

		Mockito.doReturn(true).when(federatedNetworkController).deleteFederatedNetworkFromAgent(anyString());

		ApplicationFacade.getInstance().deleteFederatedNetwork(createdFederatedNetworkId, fakeToken);

		federatedNetworks = ApplicationFacade.getInstance().getFederatedNetworks(fakeToken);
		assertEquals(0, federatedNetworks.size());
	}*/


	/*@Test
	public void testCompute() throws FederatedComputeNotFoundException, IOException,
			SubnetAddressesCapacityReachedException, UnauthenticatedUserException, InvalidParameterException {
		String cidrNotation = "10.0.0.0/24";
		String label = "testNetwork";
		String fakeToken = "fake-token";
		Set<String> allowedMembers = new HashSet<>(Arrays.asList(new String[] {"member1", "member2"}));
		FederatedNetwork federatedNetwork = new FederatedNetwork(cidrNotation, label, allowedMembers, InstanceState.READY);

		doReturn(true).when(federatedNetworkController).addFederatedNetworkOnAgent(anyString(), anyString());

		String createdFederatedNetworkId = ApplicationFacade.getInstance().createFederatedNetwork(federatedNetwork, fakeToken);

		ComputeOrder computeOrder = createOrder();

		FederatedComputeOrderOld federatedComputeOrderOld = new FederatedComputeOrderOld(computeOrder, createdFederatedNetworkId);


		ComputeOrder actualComputeOrder = ApplicationFacade.getInstance().
				addFederatedIpInGetInstanceIfApplied(federatedComputeOrderOld, fakeToken);

		assertEquals(computeOrder.getId(), actualComputeOrder.getId());

		UserData userData = actualComputeOrder.getUserData();

		assertTrue(userData != null);

		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.LEFT_SOURCE_IP_KEY));
		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.RIGHT_IP));
		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.RIGHT_SUBNET_KEY));
		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.IS_FEDERATED_VM_KEY));

		ComputeInstance computeInstance = new ComputeInstance(fakeId, InstanceState.READY, "host", 2,
				1024, 30,"fake-ip");

		ComputeInstance newComputeInstance = ApplicationFacade.getInstance().
				addFederatedIpInGetInstanceIfApplied(computeInstance, fakeToken);

		assertTrue(newComputeInstance instanceof FederatedComputeInstance);
		FederatedComputeInstance federatedComputeInstance = (FederatedComputeInstance) newComputeInstance;
		String federatedIp = federatedComputeInstance.getFederatedIp();

		assertFalse(federatedComputeInstance.getFederatedIp().isEmpty());

		FederationUser federationUser = Mockito.mock(FederationUser.class);
		federatedNetwork = federatedNetworkController.getFederatedNetwork(createdFederatedNetworkId, federationUser);

		assertFalse(federatedNetwork.isIpAddressFree(federatedIp));

		federatedNetwork.freeIp(federatedIp, federatedComputeInstance.getId());

		assertTrue(federatedNetwork.isIpAddressFree(federatedIp));
	}*/

	private ComputeOrder createOrder() {
		FederationUser federationUser = Mockito.mock(FederationUser.class);
		UserData userData = Mockito.mock(UserData.class);
		String imageId = "fake-image-id";
		String publicKey = "fake-public-key";
		String fakeMember = "fake-member";
		List<String> networksId = Arrays.asList(new String[]{"fake-net-id"});

		ComputeOrder localOrder =
				new ComputeOrder(
						fakeId,
						federationUser,
						fakeMember,
						fakeMember,
						2,
						1024,
						30,
						imageId,
						userData,
						publicKey,
						networksId);
		return localOrder;
	}

}
