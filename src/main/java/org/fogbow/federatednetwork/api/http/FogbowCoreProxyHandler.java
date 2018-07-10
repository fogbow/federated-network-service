package org.fogbow.federatednetwork.api.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.exceptions.FederatedComputeNotFoundException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbowcloud.manager.api.http.ComputeOrdersController;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.manager.core.exceptions.UnexpectedException;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
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
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;


@Controller
public class FogbowCoreProxyHandler {

	// TODO: Check the if path variable of federatedNetworkId works

	// TODO: The header key should be retrieved from ComputeOrdersController
	private static final String FEDERATION_TOKEN_VALUE_HEADER_KEY = "federationTokenValue";

	public static final int PORT = 8080;
	public static final String SERVER = "localhost";

	@RequestMapping("/**")
	public ResponseEntity captureRestRequest(@RequestBody(required = false) String body,
	                               HttpMethod method, HttpServletRequest request) throws
			URISyntaxException, IOException, SubnetAddressesCapacityReachedException, FederatedComputeNotFoundException,
			UnauthenticatedUserException, UnexpectedException {

		final String requestUrl = request.getRequestURI();

		// FIXME check if this works
		if (requestUrl.startsWith("/" + ComputeOrdersController.COMPUTE_ENDPOINT)) {
			switch (method) {
				case POST:
					return processPostCompute(body, method, request);
				case GET:
					if (request.getRequestURI().equals("/" + ComputeOrdersController.COMPUTE_ENDPOINT)) { // if it is a get all
						return processGetAllCompute(body, method, request);
					} else { // if it is a get by id
						return processGetByIdCompute(body, method, request);
					}
				case DELETE:
					return processDeleteCompute(body, method, request);
			}
		}

		return redirectRequest(body, method, request, String.class);
	}

	private <T> ResponseEntity<T> redirectRequest(String body, HttpMethod method, HttpServletRequest request, Class<T> responseType)
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

	private ResponseEntity processPostCompute(String body, HttpMethod method, HttpServletRequest request) throws
			FederatedComputeNotFoundException, SubnetAddressesCapacityReachedException,
			IOException, URISyntaxException, UnauthenticatedUserException, UnexpectedException {

		String federationTokenValue = request.getHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY);

		final Gson gson = new Gson();

		FederatedComputeOrder federatedComputeOrder = gson.fromJson(body, FederatedComputeOrder.class);
		ComputeOrder incrementedComputeOrder = ApplicationFacade.getInstance().addFederatedAttributesIfApplied(
				federatedComputeOrder, federationTokenValue);

		ResponseEntity<String> responseEntity = redirectRequest(gson.toJson(incrementedComputeOrder), method, request, String.class);
		if (responseEntity.getStatusCode().value() >= HttpStatus.MULTIPLE_CHOICES.value()) {
			return responseEntity;
		}
		// Once fogbow-core generates a new UUID for each request, we need to sync the ID created in federated-network,
		// with the one created in fogbow-core, thats why we run an "updateOrderId" method.
		String responseOrderId = responseEntity.getBody();
		ApplicationFacade.getInstance().updateOrderId(federatedComputeOrder,responseOrderId, federationTokenValue);
		return responseEntity;
	}

	private ResponseEntity<ComputeInstance> processGetByIdCompute(String body, HttpMethod method, HttpServletRequest request)
			throws URISyntaxException, FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {

		String federationTokenValue = request.getHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY);
		ResponseEntity<String> response = redirectRequest(body, method, request, String.class);
		ComputeInstance computeInstance = new Gson().fromJson(response.getBody(), ComputeInstance.class);

		if (computeInstance != null) {
			ComputeInstance incrementedComputeInstance = ApplicationFacade.getInstance().addFederatedAttributesIfApplied(computeInstance, federationTokenValue);
			return new ResponseEntity(incrementedComputeInstance, HttpStatus.OK);
		}
		return new ResponseEntity(computeInstance, HttpStatus.OK);
	}

	private ResponseEntity<List<ComputeInstance>> processGetAllCompute(String body, HttpMethod method, HttpServletRequest request)
			throws URISyntaxException, FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {

		String federationTokenValue = request.getHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY);
		ResponseEntity<String> response = redirectRequest(body, method, request, String.class);
		Gson gson = new Gson();
		Type ComputeInstanceListType = new TypeToken<List<ComputeInstance>>() {}.getType();

		List<ComputeInstance> computeInstances = new Gson().fromJson(response.getBody(), ComputeInstanceListType);

		for (int i = 0; i < computeInstances.size(); i++) {
			ComputeInstance computeInstance = computeInstances.get(i);
			if (computeInstance != null) {
				ComputeInstance incrementedComputeInstance = ApplicationFacade.getInstance().
						addFederatedAttributesIfApplied(computeInstance, federationTokenValue);
				computeInstances.set(i, incrementedComputeInstance);
			}
		}
		return new ResponseEntity(computeInstances, HttpStatus.OK);
	}

	private ResponseEntity<String> processDeleteCompute(@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request)
			throws FederatedComputeNotFoundException, URISyntaxException, UnauthenticatedUserException, UnexpectedException {

		String federationTokenValue = request.getHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY);

		String queryString = request.getRequestURI().replace(ComputeOrdersController.COMPUTE_ENDPOINT, "");
		queryString = queryString.replace("/", "");


		ApplicationFacade.getInstance().deleteCompute(queryString, federationTokenValue);

		return redirectRequest(body, method, request, String.class);
	}

	private class NoOpErrorHandler implements ResponseErrorHandler {

		@Override
		public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
			return clientHttpResponse.getRawStatusCode() >= 300;
		}

		@Override
		public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
		}

	}

	private class PostComputeBody {

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
