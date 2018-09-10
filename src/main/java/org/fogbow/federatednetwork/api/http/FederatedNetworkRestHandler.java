package org.fogbow.federatednetwork.api.http;

import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.exceptions.AgentCommucationException;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.ras.api.http.ComputeOrdersController;
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

    public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";

    @PostMapping
    public static final ResponseEntity<String> createFederatedNetwork(@RequestBody FederatedNetworkOrder
                              federatedNetwork, @RequestHeader(required = false, value =
            ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException, InvalidCidrException,
            AgentCommucationException, UnavailableProviderException, UnauthorizedRequestException, SQLException {

        final String federatedNetworkId = ApplicationFacade.getInstance().createFederatedNetwork(federatedNetwork,
                federationTokenValue);
        return new ResponseEntity<>(federatedNetworkId, HttpStatus.CREATED);
    }

    @GetMapping(value = "/" + ComputeOrdersController.STATUS_ENDPOINT)
    public static final ResponseEntity<Collection<InstanceStatus>> getFederatedNetworksStatus(
            @RequestHeader(required = false, value = ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws UnauthenticatedUserException, InvalidParameterException,
            UnavailableProviderException, UnauthorizedRequestException {

        final Collection<InstanceStatus> federatedNetworks = ApplicationFacade.getInstance().
                getFederatedNetworksStatus(federationTokenValue);
        return federatedNetworks == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(federatedNetworks);
    }

    @GetMapping(value = "/{federatedNetworkId}")
    public static ResponseEntity<FederatedNetworkOrder> getFederatedNetwork(@PathVariable String federatedNetworkId,
            @RequestHeader(required = false, value = ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws UnauthenticatedUserException, InvalidParameterException,
            UnavailableProviderException, UnauthorizedRequestException {

        try {
            final FederatedNetworkOrder federatedNetwork = ApplicationFacade.getInstance().
                    getFederatedNetwork(federatedNetworkId, federationTokenValue);
            return ResponseEntity.ok(federatedNetwork);
        } catch (FederatedNetworkNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/{federatedNetworkId}")
    public static ResponseEntity<String> deleteFederatedNetwork(@PathVariable String federatedNetworkId,
            @RequestHeader(required = false, value = ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws NotEmptyFederatedNetworkException, UnauthenticatedUserException,
            InvalidParameterException, FederatedNetworkNotFoundException, AgentCommucationException,
            UnavailableProviderException, UnauthorizedRequestException, SQLException {

        ApplicationFacade.getInstance().deleteFederatedNetwork(federatedNetworkId, federationTokenValue);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
