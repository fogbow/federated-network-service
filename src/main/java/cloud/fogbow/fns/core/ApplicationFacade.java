package cloud.fogbow.fns.core;

import cloud.fogbow.as.core.util.AuthenticationUtil;
import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.InvalidParameterException;
import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.exceptions.InternalServerErrorException;
import cloud.fogbow.common.http.ExceptionResponse;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.plugins.authorization.AuthorizationPlugin;
import cloud.fogbow.common.util.CryptoUtil;
import cloud.fogbow.common.util.connectivity.HttpErrorConditionToFogbowExceptionMapper;
import cloud.fogbow.common.util.ServiceAsymmetricKeysHolder;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.api.http.response.ResourceId;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.drivers.AgentConfiguration;
import cloud.fogbow.fns.core.drivers.ServiceDriver;
import cloud.fogbow.fns.core.model.*;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.fns.utils.RedirectToRasUtil;
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
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApplicationFacade {
    private final Logger LOGGER = Logger.getLogger(ApplicationFacade.class);
    private Gson gson = new Gson();
    private final String FAILED_REQUEST_BODY = "{\"message\":\"" + "%s" + "\"}";

    private static ApplicationFacade instance;
    private FederatedNetworkOrderController federatedNetworkOrderController;
    private ComputeRequestsController computeRequestsController;
    private AuthorizationPlugin<FnsOperation> authorizationPlugin;
    private RSAPublicKey asPublicKey;
    private String buildNumber;
    private ServiceListController serviceListController;

    private ApplicationFacade() {
        this.asPublicKey = null;
        this.buildNumber = PropertiesHolder.getInstance().getPropertyOrDefault(ConfigurationPropertyKeys.BUILD_NUMBER_KEY, ConfigurationPropertyDefaults.BUILD_NUMBER);
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
    public String getPublicKey() throws InternalServerErrorException {
        // There is no need to authenticate the user or authorize this operation
        try {
            return CryptoUtil.toBase64(ServiceAsymmetricKeysHolder.getInstance().getPublicKey());
        } catch (GeneralSecurityException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    // federated network requests need not be synchronized because synchronization is done at the order object level
    // (see FederatedNetworkOrderController).
    public String createFederatedNetwork(FederatedNetworkOrder order, String systemUserToken)
            throws FogbowException {
        ServiceListController serviceListController = new ServiceListController();
        if(!serviceListController.getServiceNames().contains(order.getServiceName())) {
            throw new InvalidParameterException(String.format(Messages.Exception.NOT_SUPPORTED_SERVICE_S, order.getServiceName()));
        }

        // Check order consistency
        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(order.getCidr());
        if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
            LOGGER.error(String.format(Messages.Exception.INVALID_CIDR_S, order.getCidr()));
            throw new InvalidParameterException(String.format(Messages.Exception.INVALID_CIDR_S, order.getCidr()));
        }
        // Check if the user is authentic
        SystemUser requester = authenticate(systemUserToken);
        // Set requester field in the order
        order.setSystemUser(requester);
        // Check if the authenticated user is authorized to perform the requested operation
        this.authorizationPlugin.isAuthorized(requester, new FnsOperation(Operation.CREATE, ResourceType.FEDERATED_NETWORK, order));
        // Add order to the poll of active orders and to the OPEN linked list
        this.federatedNetworkOrderController.activateOrder(order);
        return order.getId();
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, String systemUserToken)
            throws FogbowException {
        SystemUser systemUser = authenticate(systemUserToken);
        FederatedNetworkOrder order = this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId);
        authorizeOrder(systemUser, Operation.GET, ResourceType.FEDERATED_NETWORK, order);
        return order;
    }

    public Collection<InstanceStatus> getFederatedNetworksStatus(String systemUserToken)
            throws FogbowException {
        SystemUser systemUser = authenticate(systemUserToken);
        this.authorizationPlugin.isAuthorized(systemUser, new FnsOperation(Operation.GET_ALL, ResourceType.FEDERATED_NETWORK));
        return this.federatedNetworkOrderController.getInstancesStatus(systemUser);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String systemUserToken)
            throws FogbowException {
        SystemUser systemUser = authenticate(systemUserToken);
        FederatedNetworkOrder order = this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId);
        authorizeOrder(systemUser, Operation.DELETE, ResourceType.FEDERATED_NETWORK, order);
        this.federatedNetworkOrderController.deleteFederatedNetwork(order);
    }

    // federatedCompute requests that involve federated network need to be synchronized because there is no order object to
    // synchronize to.
    public synchronized String createCompute(FederatedCompute federatedCompute, String systemUserToken)
            throws FogbowException {
        // Authentication and authorization is performed by the RAS.
        String federatedNetworkId = federatedCompute.getFederatedNetworkId();
        FederatedNetworkOrder federatedNetworkOrder = null;
        String instanceIp = null;

        if (federatedNetworkId != null) {
            federatedNetworkOrder = this.federatedNetworkOrderController.getFederatedNetwork(federatedNetworkId);
            if(!federatedNetworkOrder.getSystemUser().equals(authenticate(systemUserToken))) {
                throw new UnauthorizedRequestException();
            }
            instanceIp = federatedNetworkOrder.getFreeIp();
            String serviceName = federatedNetworkOrder.getServiceName();
            ServiceDriver driver = new ServiceDriverConnector(serviceName).getDriver();
            AgentConfiguration agentConfiguration = driver.configureAgent(federatedCompute.getCompute().getProvider());
            UserData userData = driver.getComputeUserData(agentConfiguration, federatedCompute, federatedNetworkOrder, instanceIp);
            addUserData(federatedCompute, userData);
        }

        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            String body = gson.toJson(federatedCompute.getCompute());
            responseEntity = RedirectToRasUtil.createAndSendRequestToRas("/" + Compute.COMPUTE_ENDPOINT, body,
                    HttpMethod.POST, systemUserToken, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(String.format(FAILED_REQUEST_BODY, Messages.Exception.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND));
        }
        // if response status was not successful, return the status and rollback, undoing the latest modifications
        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorConditionToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
        }
        ResourceId computeId = gson.fromJson(responseEntity.getBody(), ResourceId.class);
        if(federatedNetworkId != null) {
            LOGGER.info("Adding: " + computeId.getId() + ", " + federatedCompute.getCompute().getProvider() + ", " + instanceIp);
            AssignedIp assignedIp = new AssignedIp(computeId.getId(), federatedCompute.getCompute().getProvider(), instanceIp);
            this.computeRequestsController.addIpToComputeAllocation(assignedIp, federatedCompute.getFederatedNetworkId());
        }

        return computeId.getId();
    }

    public synchronized void deleteCompute(String computeId, String systemUserToken) throws FogbowException{
        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId), "",
                    HttpMethod.DELETE, systemUserToken, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(String.format(FAILED_REQUEST_BODY, Messages.Exception.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND));
        }

        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorConditionToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
        }

        FederatedNetworkOrder federatedNetworkOrder = this.computeRequestsController.getFederatedNetworkOrderAssociatedToCompute(computeId);

        if(federatedNetworkOrder != null) {
            AssignedIp assignedIp = federatedNetworkOrder.removeAssociatedIp(computeId);
            ComputeIdToFederatedNetworkIdMapping.getInstance().remove(computeId);
            ServiceDriver driver = new ServiceDriverConnector(federatedNetworkOrder.getServiceName()).getDriver();
            LOGGER.info("Delete: " + assignedIp.getProviderId() + ", " + assignedIp.getComputeId() + ", " + assignedIp.getIp());
            driver.cleanupAgent(assignedIp.getProviderId(), federatedNetworkOrder, assignedIp.getIp());
        }
    }

    public synchronized ComputeInstance getComputeById(String computeId, String systemUserToken)
            throws FogbowException {
        // Authentication and authorization is performed by the RAS.
        ResponseEntity<String> responseEntity = null;
        // We need a try-catch here, because a connect exception may be thrown, if RAS is offline.
        try {
            responseEntity = RedirectToRasUtil.createAndSendRequestToRas(("/" + Compute.COMPUTE_ENDPOINT + "/" + computeId),
                    "", HttpMethod.GET, systemUserToken, String.class);
        } catch (RestClientException e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_GATEWAY).
                    body(String.format(FAILED_REQUEST_BODY, Messages.Exception.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND));
        }

        if (responseEntity.getStatusCodeValue() >= HttpStatus.MULTIPLE_CHOICES.value()) {
            // Note that if an error occurs, the IP that was removed from the cached list does not need to be returned,
            // since it is eventually recovered when the cached list gets empty and is later refilled.
            ExceptionResponse response = gson.fromJson(responseEntity.getBody(), ExceptionResponse.class);
            throw HttpErrorConditionToFogbowExceptionMapper.map(responseEntity.getStatusCode().value(), response.getMessage());
        }
        ComputeInstance computeInstance = gson.fromJson(responseEntity.getBody(), ComputeInstance.class);
        this.computeRequestsController.addFederatedIpInGetInstanceIfApplied(computeInstance, computeId);
        return computeInstance;
    }

    public List<String> getServiceNames(String systemUserToken) throws FogbowException{
        SystemUser requester = authenticate(systemUserToken);
        FnsOperation fnsOperation = new FnsOperation(Operation.GET, ResourceType.SERVICE_NAMES);
        this.authorizationPlugin.isAuthorized(requester, fnsOperation);
        return this.serviceListController.getServiceNames();
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
        FederatedNetworkOrder order) throws InternalServerErrorException, UnauthorizedRequestException {
        // Check whether requester owns order
        SystemUser orderOwner = order.getSystemUser();
        if (!orderOwner.equals(requester)) {
            throw new UnauthorizedRequestException(Messages.Exception.REQUESTER_DOES_NOT_OWN_REQUEST);
        }
        this.authorizationPlugin.isAuthorized(requester, new FnsOperation(operation, type, order));
    }

    protected void addUserData(FederatedCompute compute, UserData userData) {
        cloud.fogbow.ras.api.parameters.Compute rasCompute = compute.getCompute();
        List<UserData> userDataList = rasCompute.getUserData();

        if (userDataList == null) {
            userDataList = new ArrayList<>();
            rasCompute.setUserData((ArrayList<UserData>) userDataList);
        }

        userDataList.add(userData);
    }

    public ServiceListController getServiceListController() {
        return serviceListController;
    }

    public void setServiceListController(ServiceListController serviceListController) {
        this.serviceListController = serviceListController;
    }

    protected SystemUser authenticate(String userToken) throws FogbowException {
        RSAPublicKey keyRSA = getAsPublicKey();
        return AuthenticationUtil.authenticate(keyRSA, userToken);
    }
}