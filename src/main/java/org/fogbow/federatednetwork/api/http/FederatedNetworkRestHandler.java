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

	@RequestMapping(method = POST)
	public static final ResponseEntity<String> createFederatedNetwork(@RequestBody FederatedNetwork federatedNetwork,
	                                                                  @RequestHeader("federationTokenValue") String federationTokenValue)
			throws OrderManagementException, UnauthorizedException, UnauthenticatedException {

		final String federatedNetworkId = ApplicationFacade.getInstance().createFederatedNetwork(federatedNetwork, federationTokenValue);
		return new ResponseEntity<>(federatedNetworkId, HttpStatus.CREATED);
	}

	@RequestMapping(method = GET)
	public static final ResponseEntity<Collection<FederatedNetwork>> getFederatedNetworks(@RequestHeader("federationTokenValue") String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException {

		final Collection<FederatedNetwork> federatedNetworks = ApplicationFacade.getInstance().getFederatedNetworks(federationTokenValue);
		return new ResponseEntity<>(federatedNetworks, HttpStatus.OK);
	}

	@RequestMapping(value = "{id}", method = GET)
	public static final ResponseEntity<FederatedNetwork> getFederatedNetwork(@PathVariable String federatedNetworkId,
	                                                                         @RequestHeader("federationTokenValue") String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException, FederatedComputeNotFoundException {

		final FederatedNetwork federatedNetwork = ApplicationFacade.getInstance().getFederatedNetwork(federatedNetworkId, federationTokenValue);
		return new ResponseEntity<>(federatedNetwork, HttpStatus.OK);
	}

	@RequestMapping(value = "{id}", method = DELETE)
	public static final ResponseEntity<String> deleteFederatedNetwork(@PathVariable String federatedNetworkId,
	                                                                  @RequestHeader("federationTokenValue") String federationTokenValue)
			throws UnauthenticatedException, UnauthorizedException, NotEmptyFederatedNetworkException, FederatedComputeNotFoundException {

		ApplicationFacade.getInstance().deleteFederatedNetwork(federatedNetworkId, federationTokenValue);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
