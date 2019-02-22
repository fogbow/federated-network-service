package cloud.fogbow.fns.utils;

import cloud.fogbow.common.constants.FogbowConstants;
import cloud.fogbow.common.exceptions.*;
import cloud.fogbow.common.util.ServiceAsymmetricKeysHolder;
import cloud.fogbow.common.util.TokenValueProtector;
import cloud.fogbow.fns.api.http.request.Redirection;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.PublicKeysHolder;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.ras.api.http.CommonKeys;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;

public class RedirectUtil {

    private static final Logger LOGGER = Logger.getLogger(Redirection.class);

    public static <T> ResponseEntity<T> redirectRequest(String body, HttpMethod method, HttpServletRequest request,
                                                        Class<T> responseType) throws FatalErrorException,
            FogbowException {
        String requestUrl = request.getRequestURI();
        String rasUrl = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.RAS_URL_KEY);
        int rasPort = Integer.parseInt(PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.RAS_PORT_KEY));

        URI uri = null;
        try {
            uri = new URI(rasUrl);
        } catch (URISyntaxException e) {
            throw new ConfigurationErrorException(String.format(Messages.Exception.INVALID_URL, rasUrl));
        }
        uri = UriComponentsBuilder.fromUri(uri).port(rasPort).path(requestUrl)
                .query(request.getQueryString()).build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY)) {
                // If the header is the federationTokenValue, then it needs to be decrypted with the FNS private key,
                // and then encrypted with the RAS public key, before being forwarded.
                RSAPrivateKey myPrivateKey = null;
                RSAPublicKey rasPublicKey = PublicKeysHolder.getInstance().getRasPublicKey();
                try {
                    myPrivateKey = ServiceAsymmetricKeysHolder.getInstance().getPrivateKey();
                } catch (IOException | GeneralSecurityException e) {
                    throw new FatalErrorException(Messages.Exception.UNABLE_TO_LOAD_PUBLIC_KEY);
                }
                String rasTokenValue = TokenValueProtector.rewrap(myPrivateKey, rasPublicKey,
                        request.getHeader(headerName), FogbowConstants.TOKEN_STRING_SEPARATOR);
                headers.set(CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY, rasTokenValue);
            } else {
                headers.set(headerName, request.getHeader(headerName));
            }
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
                                                             String federationTokenValue, Class<T> responseType) throws FatalErrorException,
            FogbowException {
        String rasUrl = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.RAS_URL_KEY);
        int rasPort = Integer.parseInt(PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.RAS_PORT_KEY));

        URI uri = null;
        try {
            uri = new URI(rasUrl);
        } catch (URISyntaxException e) {
            throw new ConfigurationErrorException(String.format(Messages.Exception.INVALID_URL, rasUrl));
        }
        uri = UriComponentsBuilder.fromUri(uri).port(rasPort).path(path).build(true).toUri();

        // The federationTokenValue needs to be decrypted with the FNS public key, and then encrypted with
        // the RAS public key, before being forwarded.
        RSAPrivateKey myPrivateKey = null;
        RSAPublicKey rasPublicKey = PublicKeysHolder.getInstance().getRasPublicKey();
        try {
            myPrivateKey = ServiceAsymmetricKeysHolder.getInstance().getPrivateKey();
        } catch (IOException | GeneralSecurityException e) {
            throw new FatalErrorException(Messages.Exception.UNABLE_TO_LOAD_PUBLIC_KEY);
        }
        String rasTokenValue = TokenValueProtector.rewrap(myPrivateKey, rasPublicKey, federationTokenValue,
                FogbowConstants.TOKEN_STRING_SEPARATOR);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set(CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY, rasTokenValue);

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
