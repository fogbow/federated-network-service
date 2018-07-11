package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.api.http.FogbowCoreProxyHandler;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.fogbow.federatednetwork.ConfigurationConstants.*;

@Component
public class Main implements ApplicationRunner {

	private static final Logger LOGGER = Logger.getLogger(Main.class);

	private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

	@Override
	public void run(ApplicationArguments args) {
		Properties properties = null;
		try {
			properties = new Properties();
			FileInputStream input = new FileInputStream(FogbowCoreProxyHandler.FEDERATED_NETWORK_CONF);
			properties.load(input);
		} catch (IOException e) {
			LOGGER.error("", e);
			System.exit(1);
		}

		String permissionFilePath = properties.getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH);
		String agentUser = properties.getProperty(FEDERATED_NETWORK_AGENT_USER);
		String agentPrivateIp = properties.getProperty(FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS);
		String agentPublicIp = properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS);

		FederatedNetworkController federatedNetworkController = new FederatedNetworkController(
				permissionFilePath, agentUser, agentPrivateIp, agentPublicIp);

		applicationFacade.setFederatedNetworkController(federatedNetworkController);
	}
}