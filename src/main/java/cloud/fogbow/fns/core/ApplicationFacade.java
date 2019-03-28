package cloud.fogbow.fns.core;

import cloud.fogbow.as.core.util.AuthenticationUtil;
import cloud.fogbow.common.exceptions.*;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.plugins.authorization.AuthorizationController;
import cloud.fogbow.common.util.HttpErrorToFogbowExceptionMapper;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.common.util.ServiceAsymmetricKeysHolder;
import cloud.fogbow.fns.api.http.response.ResourceId;
import cloud.fogbow.ras.api.http.ExceptionResponse;
import cloud.fogbow.ras.api.http.request.Compute;
import cloud.fogbow.ras.api.http.response.ComputeInstance;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.exceptions.*;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.core.model.Operation;
import cloud.fogbow.fns.core.model.ResourceType;
import cloud.fogbow.fns.utils.RedirectUtil;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;

public class ApplicationFacade {
    private final Logger LOGGER = Logger.getLogger(ApplicationFacade.class);
    private Gson gson = new Gson();

    private static ApplicationFacade instance;
    private AuthorizationController authorizationController;
    private FederatedNetworkOrderController federatedNetworkOrderController;
    private ComputeRequestsController computeRequestsController;
    private RSAPublicKey asPublicKey;
    private String buildNumber;

    private ApplicationFacade() {
        this.asPublicKey = null;
        this.buildNumber = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.BUILD_NUMBER_KEY,
                ConfigurationPropertyDefaults.BUILD_NUMBER);
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

    // public key request
    public String getPublicKey() throws UnexpectedException {
        // There is no need to authenticate the user or authorize this operation
        try {
            return CryptoUtil.savePublicKey(ServiceAsymmetricKeysHolder.getInstance().getPublicKey());
        } catch (IOException | GeneralSecurityException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    // federated network requests need not be synchronized because synchronization is done at the order object level
    // (see FederatedNetworkOrderController).
    public String createFederatedNetwork(FederatedNetworkOrder order, String systemUserToken)
            throws FogbowException,
            InvalidCidrException {
        SystemUser systemUser = AuthenticationUtil.authenticate(getAsPublicKey(), systemUserToken);

        // setting the user who is creating the federated network
        order.setSystemUser(systemUser);

        authorizeOrder(systemUser, Operation.CREATE, ResourceType.FEDERATED_NETWORK, order);
        this.federatedNetworkOrderController.addFederatedNetwork(order, systemUser);
        return order.getId();
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, String systemUserToken)
            throws FogbowException,
            FederatedNetworkNotFoundException {
        SystemUser systemUser = AuthenticationUtil.authenticate(getAsPublicKey(), systemUserToken);
        FederatedNetworkOrder order = this.federatedNetworkOrderController.getOrder(federatedNetworkId);
        authorizeOrder(systemUser, Operation.GET, ResourceType.FEDERATED_NETWORK, order);
        return this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId, systemUser);
    }

    public Collection<InstanceStatus> getFederatedNetworksStatus(String systemUserToken)
            throws FogbowException {
        SystemUser systemUser = AuthenticationUtil.authenticate(getAsPublicKey(), systemUserToken);
        this.authorizationController.authorize(systemUser, Operation.GET_ALL.getValue(),
                ResourceType.FEDERATED_NETWORK.getValue());
        return this.federatedNetworkOrderController.getFederatedNetworksStatusByUser(systemUser);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String systemUserToken)
            throws UnauthenticatedUserException, UnauthorizedRequestException, UnexpectedException,
            FederatedNetworkNotFoundException, NotEmptyFederatedNetworkException, AgentCommucationException,
            InvalidTokenException, InstanceNotFoundException {
        SystemUser systemUser = AuthenticationUtil.authenticate(this.asPublicKey, systemUserToken);
        FederatedNetworkOrder order = this.federatedNetworkOrderController.getOrder(federatedNetworkId);
        authorizeOrder(systemUser, Operation.DELETE, ResourceType.FEDERATED_NETWORK, order);
        this.federatedNetworkOrderController.deleteFederatedNetwork(federatedNetworkId, systemUser);
    }

    // compute requests that involve federated network need to be synchronized because there is no order object to
    // synchronize to.
    public synchronized String createCompute(cloud.fogbow.fns.api.parameters.Compute compute, String systemUserToken)
            throws FogbowException, IOException, InvalidCidrException, SubnetAddressesCapacityReachedException,
            FederatedNetworkNotFoundException {
        // Authentication and authorization is performed by the RAS.
        String federatedNetworkId = compute.getFederatedNetworkId();
        String instanceIp = this.computeRequestsController.addScriptToSetupTunnelIfNeeded(compute, federatedNetworkId);
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            String body = gson.toJson(compute.getCompute());
            responseEntity = RedirectUtil.createAndSendRequest("/" + Compute.COMPUTE_ENDPOINT, body,
                    HttpMethod.POST, systemUserToken, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
        }
        ResourceId computeId = gson.fromJson(responseEntity.getBody(), ResourceId.class);
        this.computeRequestsController.addIpToComputeAllocation(instanceIp, computeId.getId(), compute.getFederatedNetworkId());
        return computeId.getId();
    }

    public synchronized void deleteCompute(String computeId, String systemUserToken) throws FogbowException {
        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = RedirectUtil.createAndSendRequest(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId), "",
                    HttpMethod.DELETE, systemUserToken, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
        }
        this.computeRequestsController.removeIpToComputeAllocation(computeId);
    }

    public synchronized ComputeInstance getComputeById(String computeId, String systemUserToken)
            throws FogbowException, URISyntaxException {
        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = RedirectUtil.createAndSendRequest(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId),
                    "", HttpMethod.GET, systemUserToken, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND);
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
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

    public void setAuthorizationController(AuthorizationController authorizationController) {
        this.authorizationController = authorizationController;
    }

    public RSAPublicKey getAsPublicKey() throws FogbowException {
        if (this.asPublicKey == null) {
            this.asPublicKey = FnsPublicKeysHolder.getInstance().getAsPublicKey();
        }
        return this.asPublicKey;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    protected void authorizeOrder(SystemUser requester, Operation operation, ResourceType type,
                                  FederatedNetworkOrder order) throws UnexpectedException, UnauthorizedRequestException, InstanceNotFoundException {
        // Check whether requester owns order
        SystemUser orderOwner = order.getSystemUser();
        String ownerUserId = orderOwner.getId();
        String requestUserId = requester.getId();
        if (!ownerUserId.equals(requestUserId)) {
            throw new UnauthorizedRequestException(Messages.Exception.REQUESTER_DOES_NOT_OWN_REQUEST);
        }
        this.authorizationController.authorize(requester, operation.getValue(), type.getValue());
    }
}
