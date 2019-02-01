package cloud.fogbow.fns.api.http;

import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.core.constants.ApiDocumentation;
import cloud.fogbow.fns.core.constants.Messages;
import cloud.fogbow.fns.core.model.FederatedNetworkInstance;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.InstanceStatus;
import cloud.fogbow.ras.api.http.CommonKeys;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@CrossOrigin
@RestController
@RequestMapping(value = FederatedNetwork.FEDERATED_NETWORK_ENDPOINT)
@Api(description = ApiDocumentation.FederatedNetwork.API)
public class FederatedNetwork {
    private final Logger LOGGER = Logger.getLogger(FederatedNetwork.class);

    public static final String FEDERATED_NETWORK_ENDPOINT = "federatedNetworks";

    @ApiOperation(value = ApiDocumentation.FederatedNetwork.CREATE_OPERATION)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> createFederatedNetwork(
            @ApiParam(value = ApiDocumentation.FederatedNetwork.CREATE_REQUEST_BODY)
            @RequestBody cloud.fogbow.fns.api.parameters.FederatedNetwork federatedNetwork,
            @ApiParam(value = ApiDocumentation.CommonParameters.FEDERATION_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.CREATE_FEDERATED_NETWORK, federatedNetwork.getOrder()));
            String federatedNetworkId = ApplicationFacade.getInstance().
                    createFederatedNetwork(federatedNetwork.getOrder(), federationTokenValue);
            return new ResponseEntity<String>(federatedNetworkId, HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ApiOperation(value = ApiDocumentation.FederatedNetwork.GET_OPERATION)
    public ResponseEntity<Collection<InstanceStatus>> getFederatedNetworksStatus(
            @ApiParam(value = ApiDocumentation.CommonParameters.FEDERATION_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws Exception {

        try {
            LOGGER.info(Messages.Info.GET_FEDERATED_NETWORK_STATUS);
            Collection<InstanceStatus> federatedNetworks = ApplicationFacade.getInstance().
                    getFederatedNetworksStatus(federationTokenValue);
            return federatedNetworks == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(federatedNetworks);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    @ApiOperation(value = ApiDocumentation.FederatedNetwork.GET_BY_ID_OPERATION)
    @GetMapping(value = "/{federatedNetworkId}")
    public ResponseEntity<FederatedNetworkInstance> getFederatedNetwork(
            @ApiParam(value = ApiDocumentation.FederatedNetwork.ID)
            @PathVariable String federatedNetworkId,
            @ApiParam(value = ApiDocumentation.CommonParameters.FEDERATION_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.GET_FEDERATED_NETWORK_BY_ID,
                    (federatedNetworkId == null ? "null" : federatedNetworkId)));
            FederatedNetworkOrder federatedNetwork = ApplicationFacade.getInstance().
                    getFederatedNetwork(federatedNetworkId, federationTokenValue);
            FederatedNetworkInstance instance = federatedNetwork.getInstance();
            return new ResponseEntity<FederatedNetworkInstance>(instance, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            return ResponseEntity.notFound().build();
        }
    }

    @ApiOperation(value = ApiDocumentation.FederatedNetwork.DELETE_OPERATION)
    @DeleteMapping(value = "/{federatedNetworkId}")
    public ResponseEntity<String> deleteFederatedNetwork(
            @ApiParam(value = ApiDocumentation.FederatedNetwork.ID)
            @PathVariable String federatedNetworkId,
            @ApiParam(value = ApiDocumentation.CommonParameters.FEDERATION_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.DELETE_FEDERATED_NETWORK,
                    (federatedNetworkId == null ? "null" : federatedNetworkId)));
            ApplicationFacade.getInstance().deleteFederatedNetwork(federatedNetworkId, federationTokenValue);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }
}
