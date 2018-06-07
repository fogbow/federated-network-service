package org.fogbow.federatednetwork.api.http;

import org.fogbowcloud.manager.api.http.ComputeOrdersController;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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

	private String server = "localhost";
	private int port = 8080;

	@RequestMapping(value = ComputeOrdersController.COMPUTE_ENDPOINT, method = POST)
	public ResponseEntity<String> createCompute() {
		return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);
	}

	@RequestMapping(value = ComputeOrdersController.COMPUTE_ENDPOINT + "/{id}", method = GET)
	public ResponseEntity<String> getCompute() {
		return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);
	}

	@RequestMapping(value = ComputeOrdersController.COMPUTE_ENDPOINT + "/{id}", method = DELETE)
	public ResponseEntity<String> deleteCompute() {
		return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);
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
