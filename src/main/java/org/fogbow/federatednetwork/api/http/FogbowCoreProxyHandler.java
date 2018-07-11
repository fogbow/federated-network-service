package org.fogbow.federatednetwork.api.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.ConfigurationConstants;
import org.fogbow.federatednetwork.FederatedNetworkConstants;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

@Controller
public class FogbowCoreProxyHandler {

	private static final Logger LOGGER = Logger.getLogger(FogbowCoreProxyHandler.class);
	public static final String FEDERATED_NETWORK_CONF = "federated-network.conf";

	public String coreBaseUrl = null;
	public int corePort = -1;

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
					final String requestURI = request.getRequestURI();
					String getAllRegex = "/" + ComputeOrdersController.COMPUTE_ENDPOINT + "/?$";
					String getAllStatusRegex = "/" + ComputeOrdersController.COMPUTE_ENDPOINT + "/" +
							ComputeOrdersController.STATUS_ENDPOINT + "/?$";
					String getByIdRegex = "/" + ComputeOrdersController.COMPUTE_ENDPOINT + "/(?!" +
							ComputeOrdersController.STATUS_ENDPOINT + ").*$";
					if (requestURI.matches(getAllRegex)) {
						return processGetAllCompute(body, method, request);
					} else if (requestURI.matches(getAllStatusRegex)){
						break;
					} else if (requestURI.matches(getByIdRegex)){
						return processGetByIdCompute(body, method, request);
					}
					break;
				case DELETE:
					return processDeleteCompute(body, method, request);
			}
		}

		return redirectRequest(body, method, request, String.class);
	}

	private <T> ResponseEntity<T> redirectRequest(String body, HttpMethod method, HttpServletRequest request, Class<T> responseType)
			throws URISyntaxException {
		String requestUrl = request.getRequestURI();
		if (coreBaseUrl == null || coreBaseUrl.isEmpty() || corePort <= 0) {
			readProperties();
		}

		URI uri = new URI(FederatedNetworkConstants.HTTP, null, coreBaseUrl, corePort, null, null, null);
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

	private void readProperties() {
		Properties properties = null;
		try {
			properties = new Properties();
			FileInputStream input = new FileInputStream(FEDERATED_NETWORK_CONF);
			properties.load(input);
		} catch (IOException e) {
			LOGGER.error("", e);
			System.exit(1);
		}
		this.coreBaseUrl = properties.getProperty(ConfigurationConstants.MANAGER_CORE_BASE_URL);
		this.corePort = Integer.parseInt(properties.getProperty(ConfigurationConstants.MANAGER_CORE_PORT));
	}

	private ResponseEntity processPostCompute(String body, HttpMethod method, HttpServletRequest request) throws
			FederatedComputeNotFoundException, SubnetAddressesCapacityReachedException,
			IOException, URISyntaxException, UnauthenticatedUserException, UnexpectedException {

		String federationTokenValue = request.getHeader(ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY);

		final Gson gson = new Gson();
		FederatedComputeOrder federatedComputeOrder = gson.fromJson(body, FederatedComputeOrder.class);
		ComputeOrder incrementedComputeOrder = ApplicationFacade.getInstance().addFederatedAttributesIfApplied(
				federatedComputeOrder, federationTokenValue);

		ResponseEntity<String> responseEntity = redirectRequest(gson.toJson(incrementedComputeOrder), method, request, String.class);
		// if response status was not successful, return the status
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

		String federationTokenValue = request.getHeader(ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY);
		ResponseEntity<String> response = redirectRequest(body, method, request, String.class);
		// if response status was not successful, return the status
		if (response.getStatusCode().value() >= HttpStatus.MULTIPLE_CHOICES.value()) {
			return new ResponseEntity<ComputeInstance>(response.getStatusCode());
		}
		ComputeInstance computeInstance = new Gson().fromJson(response.getBody(), ComputeInstance.class);
		ComputeInstance incrementedComputeInstance = ApplicationFacade.getInstance().addFederatedAttributesIfApplied(computeInstance, federationTokenValue);
		return new ResponseEntity(incrementedComputeInstance, HttpStatus.OK);
	}

	private ResponseEntity<List<ComputeInstance>> processGetAllCompute(String body, HttpMethod method, HttpServletRequest request)
			throws URISyntaxException, FederatedComputeNotFoundException, UnauthenticatedUserException, UnexpectedException {

		String federationTokenValue = request.getHeader(ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY);
		ResponseEntity<String> response = redirectRequest(body, method, request, String.class);
		// if response status was not successful, return the status
		if (response.getStatusCode().value() >= HttpStatus.MULTIPLE_CHOICES.value()) {
			return new ResponseEntity<List<ComputeInstance>>(response.getStatusCode());
		}
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

		String federationTokenValue = request.getHeader(ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY);

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
}
