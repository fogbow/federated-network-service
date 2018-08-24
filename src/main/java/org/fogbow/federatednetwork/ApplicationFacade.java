package org.fogbow.federatednetwork;

import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.manager.core.AaController;
import org.fogbowcloud.manager.core.constants.Operation;
import org.fogbowcloud.manager.core.exceptions.InvalidParameterException;
import org.fogbowcloud.manager.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.manager.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.manager.core.exceptions.UnavailableProviderException;
import org.fogbowcloud.manager.core.models.InstanceStatus;
import org.fogbowcloud.manager.core.models.ResourceType;
import org.fogbowcloud.manager.core.models.instances.ComputeInstance;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;

import java.io.IOException;
import java.util.Collection;

public class ApplicationFacade {

    private static ApplicationFacade instance;

    private OrderController orderController;

    private AaController aaController;

    public synchronized static ApplicationFacade getInstance() {
        if (instance == null) {
            instance = new ApplicationFacade();
        }
        return instance;
    }

    // federated network methods

    public String createFederatedNetwork(FederatedNetworkOrder federatedNetwork, String federationTokenValue) throws
            UnauthenticatedUserException, InvalidParameterException, InvalidCidrException, AgentCommucationException,
            UnavailableProviderException, UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(federationUser, Operation.CREATE, ResourceType.NETWORK);
        return this.orderController.activateFederatedNetwork(federatedNetwork, federationUser);
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException, FederatedNetworkNotFoundException,
            UnavailableProviderException, UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(federationUser, Operation.GET, ResourceType.NETWORK);
        return this.orderController.getFederatedNetwork(federatedNetworkId, federationUser);
    }

    public Collection<InstanceStatus> getFederatedNetworksStatus(String federationTokenValue) throws
            UnauthenticatedUserException, InvalidParameterException, UnavailableProviderException,
            UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(federationUser, Operation.GET, ResourceType.NETWORK);
        return this.orderController.getUserFederatedNetworksStatus(federationUser);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws NotEmptyFederatedNetworkException, UnauthenticatedUserException, InvalidParameterException,
            FederatedNetworkNotFoundException, AgentCommucationException, UnavailableProviderException,
            UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(federationUser, Operation.DELETE, ResourceType.NETWORK);
        this.orderController.deleteFederatedNetwork(federatedNetworkId, federationUser);
    }

    // compute methods

    public ComputeOrder addFederatedIpInPostIfApplied(FederatedComputeOrder federatedComputeOrderOld,
                                                      String federationTokenValue)
            throws SubnetAddressesCapacityReachedException, IOException, UnauthenticatedUserException,
            InvalidParameterException, FederatedNetworkNotFoundException, InvalidCidrException,
            UnavailableProviderException, UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(federationUser, Operation.CREATE, ResourceType.NETWORK);
        ComputeOrder incrementedComputeOrder = this.orderController.
                addFederationUserTokenDataIfApplied(federatedComputeOrderOld, federationUser);
        return incrementedComputeOrder;
    }

    public void updateOrderId(FederatedComputeOrder federatedCompute, String newId, String federationTokenValue)
            throws UnauthenticatedUserException,  InvalidParameterException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        federatedCompute.getComputeOrder().setFederationUserToken(federationUser);
        this.orderController.updateIdOnComputeCreation(federatedCompute, newId);
    }

    public ComputeInstance addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance,
                                                                String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException, UnavailableProviderException,
            UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(federationUser, Operation.GET, ResourceType.NETWORK);
        return this.orderController.addFederatedIpInGetInstanceIfApplied(computeInstance, federationUser);
    }

    public void deleteCompute(String computeId, String federationTokenValue) throws UnauthenticatedUserException,
            InvalidParameterException, FederatedNetworkNotFoundException, UnavailableProviderException,
            UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(federationUser, Operation.CREATE, ResourceType.NETWORK);
        this.orderController.deleteCompute(computeId, federationUser);
    }

    public void rollbackInFailedPost(FederatedComputeOrder federatedCompute) {
        this.orderController.rollbackInFailedPost(federatedCompute);
    }

    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    public void setAaController(AaController aaController) {
        this.aaController = aaController;
    }
}
