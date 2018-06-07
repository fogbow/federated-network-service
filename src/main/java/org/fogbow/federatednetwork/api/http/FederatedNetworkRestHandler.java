package org.fogbow.federatednetwork.api.http;

import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = FederatedNetworkRestHandler.FEDERATED_NETWORK_ENDPOINT)
public class FederatedNetworkRestHandler {

	public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";

	@RequestMapping(method = POST)
	public static final ResponseEntity<String> createFederatedNetwork() {
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@RequestMapping(value = "{id}", method = GET)
	public static final ResponseEntity<FederatedNetwork> getFederatedNetwork() {
		return new ResponseEntity<>(new FederatedNetwork("", "", null), HttpStatus.CREATED);
	}

	@RequestMapping(value = "{id}", method = DELETE)
	public static final ResponseEntity<String> deleteFederatedNetwork() {
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
