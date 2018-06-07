package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbowcloud.manager.core.AaController;
import org.fogbowcloud.manager.core.BehaviorPluginsHolder;
import org.fogbowcloud.manager.core.CloudPluginsHolder;
import org.fogbowcloud.manager.core.services.InstantiationInitService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Main implements ApplicationRunner {

	private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

	@Override
	public void run(ApplicationArguments args) {
		InstantiationInitService instantiationInitService = new InstantiationInitService();

		// Setting up cloud plugins
		CloudPluginsHolder cloudPluginsHolder = new CloudPluginsHolder(instantiationInitService);

		// Setting up behavior plugins
		BehaviorPluginsHolder behaviorPluginsHolder = new BehaviorPluginsHolder(instantiationInitService);

		AaController aaController =
				new AaController(cloudPluginsHolder.getLocalIdentityPlugin(), behaviorPluginsHolder);

		FederatedNetworkController federatedNetworkController = new FederatedNetworkController();

		applicationFacade.setAaController(aaController);
		applicationFacade.setFederatedNetworkController(federatedNetworkController);

	}
}