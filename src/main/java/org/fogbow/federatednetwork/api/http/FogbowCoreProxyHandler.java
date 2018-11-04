package org.fogbow.federatednetwork.api.http;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.constants.ConfigurationPropertiesKeys;
import org.fogbow.federatednetwork.constants.SystemConstants;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.ras.api.http.Compute;
import org.fogbowcloud.ras.core.exceptions.InvalidParameterException;
import org.fogbowcloud.ras.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.ras.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.ras.core.exceptions.UnavailableProviderException;
import org.fogbowcloud.ras.core.models.instances.ComputeInstance;
import org.fogbowcloud.ras.core.models.orders.ComputeOrder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS,
        RequestMethod.PUT})
@Controller
public class FogbowCoreProxyHandler {
    private static final Logger LOGGER = Logger.getLogger(FogbowCoreProxyHandler.class);
    private Gson gson = new Gson();

    @RequestMapping("/**")
    public ResponseEntity captureRestRequest(@RequestBody(required = false) String body,
                                             HttpMethod method, HttpServletRequest request) throws
            URISyntaxException, IOException, SubnetAddressesCapacityReachedException,
            UnauthenticatedUserException, InvalidParameterException, FederatedNetworkNotFoundException,
            InvalidCidrException, UnavailableProviderException, UnauthorizedRequestException, SQLException {

        try {
            final String requestUrl = request.getRequestURI();

            if (requestUrl.startsWith("/" + Compute.COMPUTE_ENDPOINT)) {
                switch (method) {
                    case POST:
                        LOGGER.info(String.format(Messages.Info.CREATE_COMPUTE, requestUrl, method, body.toString(),
                                request.toString()));
                        return processPostCompute(body, method, request);
                    case GET:
                        final String requestURI = request.getRequestURI();
                        String getByIdRegex = "/" + Compute.COMPUTE_ENDPOINT + "/(?!" +
                                Compute.STATUS_ENDPOINT + "|" + Compute.QUOTA_ENDPOINT +
                                "|" + Compute.ALLOCATION_ENDPOINT + ").*$";
                        if (requestURI.matches(getByIdRegex)) {
                            LOGGER.info(String.format(Messages.Info.GET_COMPUTE_BY_ID, requestUrl, method, body.toString(),
                                    request.toString()));
                            return processGetByIdCompute(body, method, request);
                        }
                        // If it is a get in /quota or /status or /allocation, the request will be redirected to manager-core
                        break;
                    case DELETE:
                        LOGGER.info(String.format(Messages.Info.DELETE_COMPUTE, requestUrl, method, body.toString(),
                                request.toString()));
                        return processDeleteCompute(body, method, request);
                }
            }
            LOGGER.info(Messages.Info.GENERIC_REQUEST);
            return redirectRequest(body, method, request, String.class);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    private <T> ResponseEntity<T> redirectRequest(String body, HttpMethod method, HttpServletRequest request,
                                                  Class<T> responseType)
            throws URISyntaxException {
        String requestUrl = request.getRequestURI();
        Properties properties = PropertiesUtil.readProperties(SystemConstants.CONF_FILE_NAME);
        String coreBaseUrl = properties.getProperty(ConfigurationPropertiesKeys.RAS_IP);
        int corePort = Integer.parseInt(properties.getProperty(ConfigurationPropertiesKeys.RAS_PORT));

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

    private ResponseEntity processPostCompute(String body, HttpMethod method, HttpServletRequest request) throws
            InvalidParameterException, SubnetAddressesCapacityReachedException, UnauthenticatedUserException,
            IOException, URISyntaxException, FederatedNetworkNotFoundException, InvalidCidrException,
            UnavailableProviderException, UnauthorizedRequestException, SQLException {

        String federationTokenValue = request.getHeader(Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY);

        FederatedComputeOrder federatedCompute = gson.fromJson(body, FederatedComputeOrder.class);
        ComputeOrder incrementedComputeOrder = ApplicationFacade.getInstance().addFederatedIpInPostIfApplied(
                federatedCompute, federationTokenValue);

        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = redirectRequest(gson.toJson(incrementedComputeOrder), method, request, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCode().value() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            ApplicationFacade.getInstance().rollbackInFailedPost(federatedCompute);
            return responseEntity;
        }
        // Since fogbow-core generates a new UUID for each request, we need to sync the ID created in federated-network,
        // with the one created in fogbow-core, thats why we run an "updateIdOnComputeCreation" method.
        String responseOrderId = responseEntity.getBody();
        ApplicationFacade.getInstance().updateOrderId(federatedCompute, responseOrderId, federationTokenValue);
        return responseEntity;
    }

    private ResponseEntity processGetByIdCompute(String body, HttpMethod method, HttpServletRequest request)
            throws URISyntaxException, UnauthenticatedUserException,
            InvalidParameterException, UnavailableProviderException, UnauthorizedRequestException {

        String federationTokenValue = request.getHeader(Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY);
        ResponseEntity<String> response = redirectRequest(body, method, request, String.class);
        // if response status was not successful, return the status
        if (response.getStatusCode().value() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
        ComputeInstance computeInstance = gson.fromJson(response.getBody(), ComputeInstance.class);
        ComputeInstance incrementedComputeInstance = ApplicationFacade.getInstance().
                addFederatedIpInGetInstanceIfApplied(computeInstance, federationTokenValue);
        return new ResponseEntity(incrementedComputeInstance, HttpStatus.OK);
    }

    private ResponseEntity<String> processDeleteCompute(@RequestBody(required = false) String body, HttpMethod method,
                                                        HttpServletRequest request) throws URISyntaxException,
            UnauthenticatedUserException, InvalidParameterException, FederatedNetworkNotFoundException,
            UnavailableProviderException, UnauthorizedRequestException, SQLException {

        String federationTokenValue = request.getHeader(Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY);

        String queryString = request.getRequestURI().replace(Compute.COMPUTE_ENDPOINT, "");
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
