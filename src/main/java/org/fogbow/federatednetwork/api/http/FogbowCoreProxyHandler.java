package org.fogbow.federatednetwork.api.http;

import oracle.jrockit.jfr.jdkevents.ThrowableTracer;
import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbowcloud.manager.api.http.ComputeOrdersController;
import org.fogbowcloud.manager.core.exceptions.OrderManagementException;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import static org.springframework.web.bind.annotation.RequestMethod.*;


@Controller
public class FogbowCoreProxyHandler {

	// TODO: These variables should be in a configuration file.
	private String server = "localhost";
	private int port = 8080;

	// TODO: Check the if path variable of federatedNetworkId works

	@RequestMapping(value = ComputeOrdersController.COMPUTE_ENDPOINT, method = POST)
	public ResponseEntity<String> createCompute(@RequestBody ComputeOrder computeOrder, @PathVariable String federatedNetworkId,
	                                            @RequestHeader("federationTokenValue") String federationTokenValue)
			throws UnauthenticatedException, SubnetAddressesCapacityReachedException, UnauthorizedException, IOException {

		String computeId = ApplicationFacade.getInstance().createCompute(computeOrder,
				federatedNetworkId , federationTokenValue);
		return new ResponseEntity<>(computeId, HttpStatus.CREATED);
	}

	@RequestMapping(value = ComputeOrdersController.COMPUTE_ENDPOINT + "/{id}", method = GET)
	public ResponseEntity<ComputeInstance> getCompute(@PathVariable String computeId,
	                                         @RequestHeader("federationTokenValue") String federationTokenValue)
			throws FederatedComputeNotFoundException, UnauthorizedException, UnauthenticatedException {

		ComputeInstance computeInstance = ApplicationFacade.getInstance().getCompute(computeId, federationTokenValue);
		return new ResponseEntity<>(computeInstance, HttpStatus.OK);
	}

	@RequestMapping(value = ComputeOrdersController.COMPUTE_ENDPOINT + "/{id}", method = DELETE)
	public ResponseEntity<String> deleteCompute(@PathVariable String computeId,
												@RequestHeader("federationTokenValue") String federationTokenValue) {
		HttpStatus httpStatus = null;
		try {
			ApplicationFacade.getInstance().deleteCompute(computeId, federationTokenValue);
			httpStatus = HttpStatus.NO_CONTENT;
		} catch (Throwable e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(httpStatus);
	}

	@RequestMapping("/**")
	public ResponseEntity captureRestRequest(@RequestBody(required = false) String body,
									 HttpMethod method, HttpServletRequest request, HttpServletResponse response)
			throws URISyntaxException {
		String requestUrl = request.getRequestURI();

		URI uri = new URI("http", null, server, port, null, null, null);
		uri = UriComponentsBuilder.fromUri(uri).path(requestUrl)
				.query(request.getQueryString()).build(true).toUri();

		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.set(headerName, request.getHeader(headerName));
		}

		HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

		ResponseEntity<String> exchange = null;
		try {
			RestTemplate restTemplate = new RestTemplate();

			restTemplate.setErrorHandler(new ResponseErrorHandler() {

				@Override
				public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
					return clientHttpResponse.getRawStatusCode() >= 300;
				}

				@Override
				public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
				}

			});

			exchange = restTemplate.exchange(uri, method, httpEntity, String.class);
		} catch (Throwable e) {
		}

		return exchange;
	}

}
