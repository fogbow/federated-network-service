package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

import static org.junit.Assert.assertEquals;

public class ApplicationFacadeTest {

	private static final String TEST_DATABASE_FILE_PATH = "federated-networks-test.db";
	private FederatedNetworkController federatedNetworkController;

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
	public void testCreateFederationNetwork() throws UnauthenticatedException, UnauthorizedException, NotEmptyFederatedNetworkException, FederatedComputeNotFoundException {
		String cidrNotation = "10.0.0.0/24";
		String label = "testNetwork";
		Set<String> allowedMembers = new HashSet<>(Arrays.asList(new String[] {"member1", "member2"}));
		FederatedNetwork federatedNetwork = new FederatedNetwork(cidrNotation, label, allowedMembers);

		String fakeToken = "fake-token";

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

}
