package cloud.fogbow.fns.core;

import cloud.fogbow.as.core.util.AuthenticationUtil;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.common.util.HttpErrorToFogbowExceptionMapper;
import cloud.fogbow.common.util.ServiceAsymmetricKeysHolder;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.api.http.response.ResourceId;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.api.parameters.FederatedNetwork;
import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.core.model.*;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.fns.utils.RedirectToRasUtil;
import cloud.fogbow.ras.api.http.ExceptionResponse;
import cloud.fogbow.ras.api.http.request.Compute;
import cloud.fogbow.ras.api.http.response.ComputeInstance;
import cloud.fogbow.ras.core.models.UserData;
import com.google.gson.Gson;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApplicationFacade {
    private final Logger LOGGER = Logger.getLogger(ApplicationFacade.class);
    private Gson gson = new Gson();

    private static ApplicationFacade instance;
    private FederatedNetworkOrderController federatedNetworkOrderController;
    private ComputeRequestsController computeRequestsController;
    private AuthorizationPlugin<FnsOperation> authorizationPlugin;
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

        this.authorizationPlugin.isAuthorized(systemUser, new FnsOperation(Operation.CREATE, ResourceType.FEDERATED_NETWORK, order));
        this.federatedNetworkOrderController.addFederatedNetwork(order, systemUser);
        return order.getId();
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, String systemUserToken)
            throws FogbowException, FederatedNetworkNotFoundException {
        SystemUser systemUser = AuthenticationUtil.authenticate(getAsPublicKey(), systemUserToken);
        FederatedNetworkOrder order = this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId);
        authorizeOrder(systemUser, Operation.GET, ResourceType.FEDERATED_NETWORK, order);
        return order;
    }

    public Collection<InstanceStatus> getFederatedNetworksStatus(String systemUserToken)
            throws FogbowException {
        SystemUser systemUser = AuthenticationUtil.authenticate(getAsPublicKey(), systemUserToken);
        this.authorizationPlugin.isAuthorized(systemUser, new FnsOperation(Operation.GET_ALL, ResourceType.FEDERATED_NETWORK));
        return this.federatedNetworkOrderController.getFederatedNetworksStatusByUser(systemUser);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String systemUserToken)
            throws FogbowException, NotEmptyFederatedNetworkException, FederatedNetworkNotFoundException {
        SystemUser systemUser = AuthenticationUtil.authenticate(this.asPublicKey, systemUserToken);
        FederatedNetworkOrder order = this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId);
        authorizeOrder(systemUser, Operation.DELETE, ResourceType.FEDERATED_NETWORK, order);
        this.federatedNetworkOrderController.deleteFederatedNetwork(order);
    }

    // federatedCompute requests that involve federated network need to be synchronized because there is no order object to
    // synchronize to.
    public synchronized String createCompute(FederatedCompute federatedCompute, String systemUserToken)
            throws FogbowException, InvalidCidrException, SubnetAddressesCapacityReachedException,
            FederatedNetworkNotFoundException {
        // Authentication and authorization is performed by the RAS.
        String federatedNetworkId = federatedCompute.getFederatedNetworkId();
        FederatedNetworkOrder federatedNetworkOrder = null;
        String instanceIp = null;

        if (federatedNetworkId != null) {
            federatedNetworkOrder = this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId);
            instanceIp = federatedNetworkOrder.getFreeIp();
        }

        if (federatedNetworkOrder != null && instanceIp != null) {
            ConfigurationMode mode = federatedNetworkOrder.getConfigurationMode();
            String provider = federatedCompute.getCompute().getProvider();
            ServiceConnector serviceConnector = ServiceConnectorFactory.getInstance().getServiceConnector(mode, provider);

            UserData userData = serviceConnector.getTunnelCreationInitScript(instanceIp, federatedCompute, federatedNetworkOrder);
            addUserDataToCompute(federatedCompute, userData);
        }

        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            String body = gson.toJson(federatedCompute.getCompute());
            responseEntity = RedirectToRasUtil.createAndSendRequestToRas("/" + Compute.COMPUTE_ENDPOINT, body,
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
        this.computeRequestsController.addIpToComputeAllocation(instanceIp, computeId.getId(), federatedCompute.getFederatedNetworkId());
        return computeId.getId();
    }

    private void addUserDataToCompute(cloud.fogbow.fns.api.parameters.FederatedCompute compute, UserData userData) {
        cloud.fogbow.ras.api.parameters.Compute rasCompute = compute.getCompute();
        List<UserData> userDataList = rasCompute.getUserData();

        if (userDataList == null) {
            userDataList = new ArrayList<>();
            rasCompute.setUserData((ArrayList<UserData>) userDataList);
        }

        userDataList.add(userData);
    }

    public synchronized void deleteCompute(String computeId, String systemUserToken) throws FogbowException,
            URISyntaxException, InvalidCidrException {
        // NOTE(pauloewerton): since FNS has no cache of the created computes, we need to get the instance data from RAS in case
        // it was associated to a DFNS network.
        ComputeInstance computeInstance = this.getComputeById(computeId, systemUserToken);

        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId), "",
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

        FederatedNetworkOrder federatedNetworkOrder = this.computeRequestsController.getFederatedNetworkOrderAssociatedToCompute(computeId);
        this.computeRequestsController.removeIpToComputeAllocation(computeId);

        if (federatedNetworkOrder != null) {
            String hostIp = this.getComputeIpFromDefaultNetwork(computeInstance.getIpAddresses());

            this.removeAgentToComputeTunnel(federatedNetworkOrder, computeInstance.getProvider(), hostIp);
        }
    }

    public synchronized ComputeInstance getComputeById(String computeId, String systemUserToken)
            throws FogbowException, URISyntaxException {
        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId),
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

    public void setAuthorizationPlugin(AuthorizationPlugin<FnsOperation> authorizationPlugin) {
        this.authorizationPlugin = authorizationPlugin;
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
                                  FederatedNetworkOrder order) throws UnexpectedException, UnauthorizedRequestException {
        // Check whether requester owns order
        SystemUser orderOwner = order.getSystemUser();
        if (!orderOwner.equals(requester)) {
            throw new UnauthorizedRequestException(Messages.Exception.REQUESTER_DOES_NOT_OWN_REQUEST);
        }
        this.authorizationPlugin.isAuthorized(requester, new FnsOperation(operation, type, order));
    }

    private void removeAgentToComputeTunnel(FederatedNetworkOrder order, String provider, String hostIp)
            throws UnexpectedException {
        ServiceConnector serviceConnector = ServiceConnectorFactory.getInstance().getServiceConnector(order.getConfigurationMode(), provider);
        boolean isAgentToComputeTunnelRemoved = serviceConnector.removeAgentToComputeTunnel(order, hostIp);

        if (!isAgentToComputeTunnelRemoved) {
            LOGGER.warn(String.format(Messages.Warn.UNABLE_TO_DELETE_TUNNEL, hostIp, order.getVlanId()));
        }
    }

    private String getComputeIpFromDefaultNetwork(List<String> computeIps) throws InvalidCidrException {
        String defaultNetworkCidr = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.DEFAULT_NETWORK_CIDR_KEY);
        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(defaultNetworkCidr);

        for (String ip : computeIps) {
            if (subnetInfo.isInRange(ip)) {
                return ip;
            }
        }

        return null;
    }
}