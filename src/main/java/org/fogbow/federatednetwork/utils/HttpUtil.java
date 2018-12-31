package org.fogbow.federatednetwork.utils;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.api.http.Redirection;
import org.fogbow.federatednetwork.constants.ConfigurationConstants;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.constants.SystemConstants;
import org.fogbow.federatednetwork.exceptions.FatalErrorException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.fogbowcloud.ras.api.http.Compute;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

public class HttpUtil {
    private static final Logger LOGGER = Logger.getLogger(Redirection.class);

    public static <T> ResponseEntity<T> redirectRequest(String body, HttpMethod method, HttpServletRequest request,
                                                         Class<T> responseType) throws URISyntaxException, FatalErrorException {
        String requestUrl = request.getRequestURI();
        String coreBaseUrl = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_IP);
        int corePort = Integer.parseInt(PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_PORT));

        URI uri = new URI(SystemConstants.HTTP, null, coreBaseUrl, corePort, null,
                null, null);
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
        try {
            ResponseEntity<T> response = restTemplate.exchange(uri, method, httpEntity, responseType);
            return response;
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    public static <T> ResponseEntity<T> createAndSendRequest(String path, String body, HttpMethod method,
                 String federationTokenValue, Class<T> responseType) throws URISyntaxException, FatalErrorException {
        String coreBaseUrl = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_IP);
        int corePort = Integer.parseInt(PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_PORT));

        URI uri = new URI(SystemConstants.HTTP, null, coreBaseUrl, corePort, path, null, null);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set(Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY, federationTokenValue);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new NoOpErrorHandler());
        try {
            ResponseEntity<T> response = restTemplate.exchange(uri, method, httpEntity, responseType);
            return response;
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    private static class NoOpErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
            return clientHttpResponse.getRawStatusCode() >= 300;
        }

        @Override
        public void handleError(ClientHttpResponse clientHttpResponse) {
        }
    }
}
