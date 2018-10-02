package org.fogbow.federatednetwork.api.http;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@RequestMapping(value = FederatedNetwork.FEDERATED_NETWORK_ENDPOINT)
@Api(description = "Manages federated networks.")
public class FederatedNetwork {

    public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";

    @ApiOperation(value = "Creates a federated network spanning multiple cloud providers.")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> createFederatedNetwork(
            @ApiParam(value = "The parameters for the creation of a federated network are the list of IDs of the\n" +
                              "providers that will be connected, the CIDR of the network, and the name that will\n" +
                              "be given to the federated network.")
            @RequestBody org.fogbow.federatednetwork.api.parameters.FederatedNetwork federatedNetwork,
            @ApiParam(value = "This is the token that identifies a federation user.\n" +
                              "It is typically created via a call to the /tokens endpoint.")
            @RequestHeader(required = false, value =
            ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws UnauthenticatedUserException, InvalidParameterException, InvalidCidrException,
            AgentCommucationException, UnavailableProviderException, UnauthorizedRequestException, SQLException {

        final String federatedNetworkId = ApplicationFacade.getInstance().
                createFederatedNetwork(federatedNetwork.getOrder(), federationTokenValue);
        return new ResponseEntity<>(federatedNetworkId, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Lists all federated networks created by the user.")
    @GetMapping(value = "/" + ComputeOrdersController.STATUS_ENDPOINT)
    public ResponseEntity<Collection<InstanceStatus>> getFederatedNetworksStatus(
            @ApiParam(value = "This is the token that identifies a federation user.\n" +
                    "It is typically created via a call to the /tokens endpoint.")
            @RequestHeader(required = false, value = ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws UnauthenticatedUserException, InvalidParameterException,
            UnavailableProviderException, UnauthorizedRequestException {

        final Collection<InstanceStatus> federatedNetworks = ApplicationFacade.getInstance().
                getFederatedNetworksStatus(federationTokenValue);
        return federatedNetworks == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(federatedNetworks);
    }

    @ApiOperation(value = "Lists a specific federated network.")
    @GetMapping(value = "/{federatedNetworkId}")
    public ResponseEntity<FederatedNetworkOrder> getFederatedNetwork(
            @ApiParam(value = "The ID of the specific federated network.")
            @PathVariable String federatedNetworkId,
            @ApiParam(value = "This is the token that identifies a federation user.\n" +
                    "It is typically created via a call to the /tokens endpoint.")
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

    @ApiOperation(value = "Deletes a specific federated network.")
    @DeleteMapping(value = "/{federatedNetworkId}")
    public ResponseEntity<String> deleteFederatedNetwork(
            @ApiParam(value = "The ID of the specific federated network.")
            @PathVariable String federatedNetworkId,
            @ApiParam(value = "This is the token that identifies a federation user.\n" +
                    "It is typically created via a call to the /tokens endpoint.")
            @RequestHeader(required = false, value = ComputeOrdersController.FEDERATION_TOKEN_VALUE_HEADER_KEY)
                    String federationTokenValue) throws NotEmptyFederatedNetworkException, UnauthenticatedUserException,
            InvalidParameterException, FederatedNetworkNotFoundException, AgentCommucationException,
            UnavailableProviderException, UnauthorizedRequestException, SQLException {

        ApplicationFacade.getInstance().deleteFederatedNetwork(federatedNetworkId, federationTokenValue);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
