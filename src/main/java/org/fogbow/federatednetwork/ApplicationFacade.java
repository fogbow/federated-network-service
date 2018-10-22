package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.ConfigurationPropertiesDefault;
import org.fogbow.federatednetwork.constants.ConfigurationPropertiesKeys;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.constants.SystemConstants;
import org.fogbow.federatednetwork.exceptions.*;
import org.fogbow.federatednetwork.model.FederatedComputeOrder;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedUser;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.fogbowcloud.ras.core.AaaController;
import org.fogbowcloud.ras.core.constants.Operation;
import org.fogbowcloud.ras.core.exceptions.InvalidParameterException;
import org.fogbowcloud.ras.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.ras.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.ras.core.exceptions.UnavailableProviderException;
import org.fogbowcloud.ras.core.models.InstanceStatus;
import org.fogbowcloud.ras.core.models.ResourceType;
import org.fogbowcloud.ras.core.models.instances.ComputeInstance;
import org.fogbowcloud.ras.core.models.orders.ComputeOrder;
import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

public class ApplicationFacade {
    private final Logger LOGGER = Logger.getLogger(ApplicationFacade.class);

    private static ApplicationFacade instance;
    private OrderController orderController;
    private AaaController aaController;
    private String memberId;

    private ApplicationFacade() {
        Properties properties = PropertiesUtil.readProperties();
        this.memberId = properties.getProperty(ConfigurationPropertiesKeys.RAS_NAME);
    }

    public synchronized static ApplicationFacade getInstance() {
        if (instance == null) {
            instance = new ApplicationFacade();
        }
        return instance;
    }

    // version request
    public String getVersionNumber() throws FileNotFoundException {
        return getVersionNumber(SystemConstants.CONF_FILE_NAME);
    }

    // Used in tests only
    protected String getVersionNumber(String filePath) throws FileNotFoundException {
        return SystemConstants.API_VERSION_NUMBER + "-" + readBuildFromFile(filePath);
    }

    private String readBuildFromFile(String membershipConfPath) throws FileNotFoundException {
        Properties properties = new Properties();
        InputStream input = new FileInputStream(membershipConfPath);
        String build = "empty";

        try {
            properties.load(input);

            build = properties.getProperty(ConfigurationPropertiesKeys.BUILD_NUMBER,
                    ConfigurationPropertiesDefault.BUILD_NUMBER);
        } catch (IOException e) {
            LOGGER.warn(String.format(Messages.Warn.ERROR_READING_CONF_FILE, membershipConfPath), e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                LOGGER.warn(String.format(Messages.Warn.ERROR_CLOSING_CONF_FILE, membershipConfPath), e);
            }
        }

        return build;
    }

    // federated network methods

    public String createFederatedNetwork(FederatedNetworkOrder federatedNetwork, String federationTokenValue) throws
            UnauthenticatedUserException, InvalidParameterException, InvalidCidrException, AgentCommucationException,
            UnavailableProviderException, UnauthorizedRequestException, SQLException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.CREATE, ResourceType.NETWORK);
        return this.orderController.activateFederatedNetwork(federatedNetwork, federationUser);
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException, FederatedNetworkNotFoundException,
            UnavailableProviderException, UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        FederatedUser user = new FederatedUser(federationUser.getUserId(), federationUser.getUserName());
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.GET, ResourceType.NETWORK);
        return this.orderController.getFederatedNetwork(federatedNetworkId, user);
    }

    public Collection<InstanceStatus> getFederatedNetworksStatus(String federationTokenValue) throws
            UnauthenticatedUserException, InvalidParameterException, UnavailableProviderException,
            UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        FederatedUser user = new FederatedUser(federationUser.getUserId(), federationUser.getUserName());
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.GET, ResourceType.NETWORK);
        return this.orderController.getUserFederatedNetworksStatus(user);
    }

    public void deleteFederatedNetwork(String federatedNetworkId, String federationTokenValue)
            throws NotEmptyFederatedNetworkException, UnauthenticatedUserException, InvalidParameterException,
            FederatedNetworkNotFoundException, AgentCommucationException, UnavailableProviderException,
            UnauthorizedRequestException, SQLException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        FederatedUser user = new FederatedUser(federationUser.getUserId(), federationUser.getUserName());
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.DELETE, ResourceType.NETWORK);
        this.orderController.deleteFederatedNetwork(federatedNetworkId, user);
    }

    // compute methods

    public ComputeOrder addFederatedIpInPostIfApplied(FederatedComputeOrder federatedComputeOrderOld,
                                                      String federationTokenValue)
            throws SubnetAddressesCapacityReachedException, IOException, UnauthenticatedUserException,
            InvalidParameterException, FederatedNetworkNotFoundException, InvalidCidrException,
            UnavailableProviderException, UnauthorizedRequestException, SQLException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.CREATE, ResourceType.NETWORK);
        ComputeOrder incrementedComputeOrder = this.orderController.
                addFederationUserTokenDataIfApplied(federatedComputeOrderOld, federationUser);
        return incrementedComputeOrder;
    }

    public void updateOrderId(FederatedComputeOrder federatedCompute, String newId, String federationTokenValue)
            throws InvalidParameterException, SQLException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        federatedCompute.getComputeOrder().setFederationUserToken(federationUser);
        this.orderController.updateIdOnComputeCreation(federatedCompute, newId);
    }

    public ComputeInstance addFederatedIpInGetInstanceIfApplied(ComputeInstance computeInstance,
                                                                String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException, UnavailableProviderException,
            UnauthorizedRequestException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        FederatedUser user = new FederatedUser(federationUser.getUserId(), federationUser.getUserName());
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.GET, ResourceType.NETWORK);
        return this.orderController.addFederatedIpInGetInstanceIfApplied(computeInstance, user);
    }

    public void deleteCompute(String computeId, String federationTokenValue) throws UnauthenticatedUserException,
            InvalidParameterException, FederatedNetworkNotFoundException, UnavailableProviderException,
            UnauthorizedRequestException, SQLException {
        FederationUserToken federationUser = this.aaController.getFederationUser(federationTokenValue);
        FederatedUser user = new FederatedUser(federationUser.getUserId(), federationUser.getUserName());
        this.aaController.authenticateAndAuthorize(this.memberId, federationUser, Operation.CREATE, ResourceType.NETWORK);
        this.orderController.deleteCompute(computeId, user);
    }

    public void rollbackInFailedPost(FederatedComputeOrder federatedCompute) throws SQLException {
        this.orderController.rollbackInFailedPost(federatedCompute);
    }

    public void setOrderController(OrderController orderController) {
        this.orderController = orderController;
    }

    public void setAaController(AaaController aaController) {
        this.aaController = aaController;
    }
}
