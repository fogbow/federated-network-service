package org.fogbow.federatednetwork.api.http;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.exceptions.AgentCommucationException;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.ras.api.http.Compute;
import org.fogbowcloud.ras.core.exceptions.InvalidParameterException;
import org.fogbowcloud.ras.core.exceptions.UnauthenticatedUserException;
import org.fogbowcloud.ras.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.ras.core.exceptions.UnavailableProviderException;
import org.fogbowcloud.ras.core.models.InstanceStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Collection;

@CrossOrigin
@RestController
@RequestMapping(value = FederatedNetworkRestHandler.FEDERATED_NETWORK_ENDPOINT)
public class FederatedNetworkRestHandler {
    private final static Logger LOGGER = Logger.getLogger(FederatedNetworkRestHandler.class);

    public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";

    @PostMapping
    public static final ResponseEntity<String> createFederatedNetwork(@RequestBody FederatedNetworkOrder
                              federatedNetwork, @RequestHeader(required = false, value =
            Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException, InvalidCidrException,
            AgentCommucationException, UnavailableProviderException, UnauthorizedRequestException, SQLException {

        try {
            LOGGER.info(String.format(Messages.Info.CREATE_FEDERATED_NETWORK,
                    (federationTokenValue == null ? "null" : federationTokenValue.toString())));
            final String federatedNetworkId = ApplicationFacade.getInstance().createFederatedNetwork(federatedNetwork,
                    federationTokenValue);
            return new ResponseEntity<>(federatedNetworkId, HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    @GetMapping(value = "/" + Compute.STATUS_ENDPOINT)
    public static final ResponseEntity<Collection<InstanceStatus>> getFederatedNetworksStatus(
            @RequestHeader(required = false, value = Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws UnauthenticatedUserException, InvalidParameterException,
            UnavailableProviderException, UnauthorizedRequestException {

        try {
            LOGGER.info(Messages.Info.GET_FEDERATED_NETWORK_STATUS);
            final Collection<InstanceStatus> federatedNetworks = ApplicationFacade.getInstance().
                    getFederatedNetworksStatus(federationTokenValue);
            return federatedNetworks == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(federatedNetworks);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    @GetMapping(value = "/{federatedNetworkId}")
    public static ResponseEntity<FederatedNetworkOrder> getFederatedNetwork(@PathVariable String federatedNetworkId,
            @RequestHeader(required = false, value = Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws UnauthenticatedUserException, InvalidParameterException,
            UnavailableProviderException, UnauthorizedRequestException {

        try {
            LOGGER.info(String.format(Messages.Info.GET_FEDERATED_NETWORK_BY_ID,
                    (federatedNetworkId == null ? "null" : federatedNetworkId)));
            final FederatedNetworkOrder federatedNetwork = ApplicationFacade.getInstance().
                    getFederatedNetwork(federatedNetworkId, federationTokenValue);
            return ResponseEntity.ok(federatedNetwork);
        } catch (FederatedNetworkNotFoundException e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{federatedNetworkId}")
    public static ResponseEntity<String> deleteFederatedNetwork(@PathVariable String federatedNetworkId,
            @RequestHeader(required = false, value = Compute.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws NotEmptyFederatedNetworkException, UnauthenticatedUserException,
            InvalidParameterException, FederatedNetworkNotFoundException, AgentCommucationException,
            UnavailableProviderException, UnauthorizedRequestException, SQLException {

        try {
            LOGGER.info(String.format(Messages.Info.DELETE_FEDERATED_NETWORK,
                    (federatedNetworkId == null ? "null" : federatedNetworkId)));
            ApplicationFacade.getInstance().deleteFederatedNetwork(federatedNetworkId, federationTokenValue);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (FederatedNetworkNotFoundException e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }
}
