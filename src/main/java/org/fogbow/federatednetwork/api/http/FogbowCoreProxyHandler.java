package org.fogbow.federatednetwork.api.http;

import com.google.gson.Gson;
import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbowcloud.manager.api.http.ComputeOrdersController;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedException;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.plugins.exceptions.UnauthorizedException;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;


@Controller
public class FogbowCoreProxyHandler {

	// TODO: Check the if path variable of federatedNetworkId works

	// TODO: The header key should be retrieved from ComputeOrdersController
	private static final String FEDERATION_TOKEN_VALUE_HEADER_KEY = "federationTokenValue";

	public static final int PORT = 8080;
	public static final String SERVER = "localhost";

	public <T> ResponseEntity<T> redirectRequest(String body, HttpMethod method, HttpServletRequest request, Class<T> responseType)
			throws URISyntaxException {
		String requestUrl = request.getRequestURI();

		URI uri = new URI("http", null, SERVER, PORT, null, null, null);
		uri = UriComponentsBuilder.fromUri(uri).path(requestUrl)
				.query(request.getQueryString()).build(true).toUri();

		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.set(headerName, request.getHeader(headerName));
		}

		HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new NoOpErrorHandler());
		ResponseEntity<T> response = restTemplate.exchange(uri, method, httpEntity, responseType);
		return response;
	}

	@RequestMapping("/**")
	public ResponseEntity captureRestRequest(@RequestBody(required = false) String body,
	                               HttpMethod method, HttpServletRequest request) throws
			URISyntaxException, IOException, UnauthenticatedException,
			SubnetAddressesCapacityReachedException, UnauthorizedException, FederatedComputeNotFoundException {

		final String requestUrl = request.getRequestURI();

		// FIXME check if this works
		if (requestUrl.equals(ComputeOrdersController.COMPUTE_ENDPOINT)) {
			switch (method) {
				case POST:
					return processPostCompute(body, method, request);
				case GET:
					return processGetCompute(body, method, request);
				case DELETE:
					return processDeleteCompute(body, method, request);
			}
		}

		return redirectRequest(body, method, request, String.class);
	}

	private ResponseEntity processPostCompute(String body, HttpMethod method, HttpServletRequest request) throws
			FederatedComputeNotFoundException, UnauthenticatedException, SubnetAddressesCapacityReachedException,
			UnauthorizedException, IOException, URISyntaxException {

		String federationTokenValue = request.getHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY);

		final Gson gson = new Gson();

		PostComputeBody postComputeBody = gson.fromJson(body, PostComputeBody.class);
		String federatedNetworkId = postComputeBody.getFederatedNetworkId();
		ComputeOrder requestOrder = postComputeBody.getComputeOrder();

		ComputeOrder incrementedOrder = ApplicationFacade.getInstance().addFederatedAttributesIfApplied(
				requestOrder, federatedNetworkId, federationTokenValue);

		return redirectRequest(gson.toJson(incrementedOrder), method, request, String.class);
	}

	private ResponseEntity<ComputeInstance> processGetCompute(String body, HttpMethod method, HttpServletRequest request)
			throws URISyntaxException, FederatedComputeNotFoundException, UnauthorizedException, UnauthenticatedException {

		String federationTokenValue = request.getHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY);

		ResponseEntity<ComputeInstance> response = redirectRequest(body, method, request, ComputeInstance.class);
		ComputeInstance computeInstance = response.getBody();

		if (computeInstance != null) {
			ComputeInstance incrementedComputeInstance = ApplicationFacade.getInstance().addFederatedAttributesIfApplied(computeInstance, federationTokenValue);
			return new ResponseEntity(incrementedComputeInstance, HttpStatus.OK);
		}

		return response;

	}

	private ResponseEntity<String> processDeleteCompute(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request)
			throws UnauthenticatedException, UnauthorizedException, FederatedComputeNotFoundException, URISyntaxException {

		String federationTokenValue = request.getHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY);

		ApplicationFacade.getInstance().deleteCompute(request.getQueryString(), federationTokenValue);

		return redirectRequest(body, method, request, String.class);
	}

	private static class NoOpErrorHandler implements ResponseErrorHandler {

		@Override
		public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
			return clientHttpResponse.getRawStatusCode() >= 300;
		}

		@Override
		public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
		}

	}

	class PostComputeBody {

		private ComputeOrder computeOrder;
		private String federatedNetworkId;

		public ComputeOrder getComputeOrder() {
			return computeOrder;
		}

		public void setComputeOrder(ComputeOrder computeOrder) {
			this.computeOrder = computeOrder;
		}

		public String getFederatedNetworkId() {
			return federatedNetworkId;
		}

		public void setFederatedNetworkId(String federatedNetworkId) {
			this.federatedNetworkId = federatedNetworkId;
		}
	}
}
