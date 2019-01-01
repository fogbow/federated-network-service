package org.fogbow.federatednetwork;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.DefaultConfigurationConstants;
import org.fogbow.federatednetwork.constants.ConfigurationConstants;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.constants.SystemConstants;
import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.exceptions.UnexpectedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.InstanceStatus;
import org.fogbow.federatednetwork.utils.HttpUtil;
import org.fogbow.federatednetwork.utils.PropertiesHolder;
import org.fogbowcloud.ras.core.exceptions.InvalidTokenException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import org.fogbowcloud.ras.api.http.Compute;
import org.fogbowcloud.ras.core.models.instances.ComputeInstance;

import org.fogbowcloud.ras.core.AaaController;
import org.fogbowcloud.ras.core.constants.Operation;
import org.fogbowcloud.ras.core.models.ResourceType;
import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;
import org.fogbowcloud.ras.core.exceptions.UnauthenticatedUserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;

public class ApplicationFacade {
    private final Logger LOGGER = Logger.getLogger(ApplicationFacade.class);
    private Gson gson = new Gson();

    private static ApplicationFacade instance;
    private FederatedNetworkOrderController federatedNetworkOrderController;
    private ComputeRequestsController computeRequestsController;
    private AaaController aaController;
    private String memberId;
    private String buildNumber;

    private ApplicationFacade() {
        this.memberId = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_NAME);
        this.buildNumber = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.BUILD_NUMBER,
                DefaultConfigurationConstants.BUILD_NUMBER);
    }

    public static ApplicationFacade getInstance() {
        synchronized (ApplicationFacade.class) {
            if (instance == null) {
                instance = new ApplicationFacade();
            }
            return instance;
        }
    }

    // version request
    public String getVersionNumber() {
        return SystemConstants.API_VERSION_NUMBER + "-" + this.buildNumber;
    }

    // federated network requests need not be synchronized because synchronization is done at the order object level
    // (see FederatedNetworkOrderController).
    public String createFederatedNetwork(FederatedNetworkOrder federatedNetworkOrder, String federationTokenValue)
            throws UnauthenticatedUserException, org.fogbowcloud.ras.core.exceptions.UnauthorizedRequestException,
            InvalidCidrException, InvalidTokenException, SQLException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.CREATE,
                ResourceType.NETWORK);
        this.federatedNetworkOrderController.activateFederatedNetwork(federatedNetworkOrder, federationUser);
        return federatedNetworkOrder.getId();
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws UnauthenticatedUserException, InvalidTokenException, FederatedNetworkNotFoundException,
            org.fogbowcloud.ras.core.exceptions.UnauthorizedRequestException, UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.GET, ResourceType.NETWORK);
        return this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId, federationUser);
    }

    public Collection<InstanceStatus> getFederatedNetworksStatus(String federationTokenValue) throws
            UnauthenticatedUserException, InvalidTokenException,
            org.fogbowcloud.ras.core.exceptions.UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.GET, ResourceType.NETWORK);
        return this.federatedNetworkOrderController.getUserFederatedNetworksStatus(federationUser);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws NotEmptyFederatedNetworkException, InvalidTokenException,
            FederatedNetworkNotFoundException, AgentCommucationException,
            org.fogbowcloud.ras.core.exceptions.UnauthorizedRequestException, SQLException, UnauthenticatedUserException,
            UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.DELETE,
                ResourceType.NETWORK);
        this.federatedNetworkOrderController.deleteFederatedNetwork(federatedNetworkId, federationUser);
    }

    // compute requests that involve federated network need to be synchronized because there is no order object to
    // synchronize to.
    public synchronized String createCompute(org.fogbow.federatednetwork.api.parameters.Compute compute,
                                String federationTokenValue) throws FogbowFnsException, IOException, SQLException,
                                URISyntaxException {
        // Authentication and authorization is performed by the RAS.
        String federatedNetworkId = compute.getFederatedNetworkId();
        String instanceIp = this.computeRequestsController.addScriptToSetupTunnelIfNeeded(compute, federatedNetworkId);

        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            String body = gson.toJson(compute.getCompute());
            responseEntity = HttpUtil.createAndSendRequest("/" + Compute.COMPUTE_ENDPOINT, body, HttpMethod.POST,
                    federationTokenValue, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            throw this.mappedException(responseEntity.getStatusCode(), responseEntity.getBody());
        }
        String computeId = responseEntity.getBody();
        this.computeRequestsController.addIpToComputeAllocation(instanceIp, computeId, compute.getFederatedNetworkId());
        return computeId;
    }

    public synchronized void deleteCompute(String computeId, String federationTokenValue) throws URISyntaxException,
            FogbowFnsException, SQLException {
        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = HttpUtil.createAndSendRequest(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId), "", HttpMethod.DELETE,
                    federationTokenValue, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            throw this.mappedException(responseEntity.getStatusCode(), responseEntity.getBody());
        }
        this.computeRequestsController.removeIpToComputeAllocation(computeId);
    }

    public synchronized ComputeInstance getComputeById(String computeId, String federationTokenValue)
            throws URISyntaxException, FogbowFnsException, SQLException {
        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = HttpUtil.createAndSendRequest(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId), "", HttpMethod.GET,
                    federationTokenValue, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            throw this.mappedException(responseEntity.getStatusCode(), responseEntity.getBody());
        }
        ComputeInstance computeInstance = gson.fromJson(responseEntity.getBody(), ComputeInstance.class);
        this.computeRequestsController.addFederatedIpInGetInstanceIfApplied(computeInstance, computeId);
        return computeInstance;

    }

    public void setFederatedNetworkOrderController(FederatedNetworkOrderController federatedNetworkOrderController) {
        this.federatedNetworkOrderController = federatedNetworkOrderController;
    }

    public void setComputeRequestsController(ComputeRequestsController computeRequestsController) {
        this.computeRequestsController = computeRequestsController;
    }

    public void setAaaController(AaaController aaController) {
        this.aaController = aaController;
    }

    private FogbowFnsException mappedException(HttpStatus httpCode, String message) {
        switch(httpCode) {
            case FORBIDDEN:
                return new UnauthorizedRequestException(message);
            case UNAUTHORIZED:
                return new org.fogbow.federatednetwork.exceptions.UnauthenticatedUserException(message);
            case BAD_REQUEST:
                return new org.fogbow.federatednetwork.exceptions.InvalidParameterException(message);
            case NOT_FOUND:
                return new InstanceNotFoundException(message);
            case CONFLICT:
                return new QuotaExceededException(message);
            case NOT_ACCEPTABLE:
                return new NoAvailableResourcesException(message);
            case GATEWAY_TIMEOUT:
                return new org.fogbow.federatednetwork.exceptions.UnavailableProviderException(message);
            case INTERNAL_SERVER_ERROR:
            case UNSUPPORTED_MEDIA_TYPE:
            default:
                return new UnexpectedException(message);
        }
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }
}
