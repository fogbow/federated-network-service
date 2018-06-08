package org.fogbow.federatednetwork.api.http;

import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.model.FederatedNetwork;
import org.fogbowcloud.manager.core.exceptions.OrderManagementException;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = FederatedNetworkRestHandler.FEDERATED_NETWORK_ENDPOINT)
public class FederatedNetworkRestHandler {

	public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";

	@PostMapping
	public static final ResponseEntity<String> createFederatedNetwork(@RequestBody FederatedNetwork federatedNetwork,
	                                                                  @RequestHeader("federationTokenValue") String federationTokenValue)
			throws OrderManagementException, UnauthorizedException, UnauthenticatedException {

		final String federatedNetworkId = ApplicationFacade.getInstance().createFederatedNetwork(federatedNetwork, federationTokenValue);
		return new ResponseEntity<>(federatedNetworkId, HttpStatus.CREATED);
	}

	@GetMapping
	public static final ResponseEntity<Collection<FederatedNetwork>> getFederatedNetworks(@RequestHeader("federationTokenValue") String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {

		final Collection<FederatedNetwork> federatedNetworks = ApplicationFacade.getInstance().getFederatedNetworks(federationTokenValue);
		return federatedNetworks == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(federatedNetworks);
	}

	@GetMapping(value = "/{federatedNetworkId}")
	public static ResponseEntity<FederatedNetwork> getFederatedNetwork(@PathVariable String federatedNetworkId,
	                                                                         @RequestHeader("federationTokenValue") String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException, FederatedComputeNotFoundException {

		try {
			final FederatedNetwork federatedNetwork = ApplicationFacade.getInstance().getFederatedNetwork(federatedNetworkId, federationTokenValue);
			return ResponseEntity.ok(federatedNetwork);
		} catch (FederatedComputeNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping(value = "/{federatedNetworkId}")
	public static ResponseEntity<String> deleteFederatedNetwork(@PathVariable String federatedNetworkId,
	                                                                  @RequestHeader("federationTokenValue") String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException, NotEmptyFederatedNetworkException, FederatedComputeNotFoundException {

		ApplicationFacade.getInstance().deleteFederatedNetwork(federatedNetworkId, federationTokenValue);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
