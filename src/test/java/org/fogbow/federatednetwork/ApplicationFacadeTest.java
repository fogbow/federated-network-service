package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederateComputeUtil;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeInstance;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.instances.InstanceState;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.token.FederationUser;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

		federatedNetworkController = spy(new FederatedNetworkController(
				permissionFilePath, agentUser, agentPrivateIp, agentPublicIp, TEST_DATABASE_FILE_PATH));

		ApplicationFacade.getInstance().setFederatedNetworkController(federatedNetworkController);
	}

	@After
	public void clean() {
		deleteTestFiles();
	}

	private boolean deleteTestFiles() {
		return new File(TEST_DATABASE_FILE_PATH).delete();
	}

	@Test
	public void testFederatedNetwork() throws UnauthenticatedException, UnauthorizedException, NotEmptyFederatedNetworkException, FederatedComputeNotFoundException {
		String cidrNotation = "10.0.0.0/24";
		String label = "testNetwork";
		String fakeToken = "fake-token";
		Set<String> allowedMembers = new HashSet<>(Arrays.asList(new String[] {"member1", "member2"}));
		FederatedNetwork federatedNetwork = new FederatedNetwork(cidrNotation, label, allowedMembers);

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
	}


	@Test
	public void testCompute() throws UnauthenticatedException, UnauthorizedException,
			FederatedComputeNotFoundException, IOException, SubnetAddressesCapacityReachedException {
		String cidrNotation = "10.0.0.0/24";
		String label = "testNetwork";
		String fakeToken = "fake-token";
		Set<String> allowedMembers = new HashSet<>(Arrays.asList(new String[] {"member1", "member2"}));
		FederatedNetwork federatedNetwork = new FederatedNetwork(cidrNotation, label, allowedMembers);

		doReturn(true).when(federatedNetworkController).addFederatedNetworkOnAgent(anyString(), anyString());

		String createdFederatedNetworkId = ApplicationFacade.getInstance().createFederatedNetwork(federatedNetwork, fakeToken);

		ComputeOrder computeOrder = createOrder();


		ComputeOrder actualComputeOrder = ApplicationFacade.getInstance().
				addFederatedAttributesIfApplied(computeOrder, createdFederatedNetworkId, fakeToken);

		assertEquals(computeOrder.getId(), actualComputeOrder.getId());

		UserData userData = actualComputeOrder.getUserData();

		assertTrue(userData != null);

		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.LEFT_SOURCE_IP_KEY));
		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.RIGHT_IP));
		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.RIGHT_SUBNET_KEY));
		assertTrue(!userData.getExtraUserDataFileContent().contains(FederateComputeUtil.IS_FEDERATED_VM_KEY));

		ComputeInstance computeInstance = new ComputeInstance(fakeId, "host", 8, 1024, InstanceState.READY,
				"fake-ip", "", "", "");

		ComputeInstance newComputeInstance = ApplicationFacade.getInstance().
				addFederatedAttributesIfApplied(computeInstance, fakeToken);

		assertTrue(newComputeInstance instanceof FederatedComputeInstance);
		FederatedComputeInstance federatedComputeInstance = (FederatedComputeInstance) newComputeInstance;

		assertFalse(federatedComputeInstance.getFederatedIp().isEmpty());
	}

	private ComputeOrder createOrder() {
		FederationUser federationUser = Mockito.mock(FederationUser.class);
		UserData userData = Mockito.mock(UserData.class);
		String imageName = "fake-image-name";
		String publicKey = "fake-public-key";
		String fakeMember = "fake-member";

		ComputeOrder localOrder =
				new ComputeOrder(
						fakeId,
						federationUser,
						fakeMember,
						fakeMember,
						8,
						1024,
						30,
						imageName,
						userData,
						publicKey);
		return localOrder;
	}

}
