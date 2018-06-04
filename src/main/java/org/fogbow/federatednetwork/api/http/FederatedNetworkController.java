package org.fogbow.federatednetwork.api.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = FederatedNetworkController.FEDERATED_NETWORK_ENDPOINT)
public class FederatedNetworkController {

	public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";
}
