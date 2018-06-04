package org.fogbow.federatednetwork.api.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = FederatedNetworkRestHandler.FEDERATED_NETWORK_ENDPOINT)
public class FederatedNetworkRestHandler {

	public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";

}
