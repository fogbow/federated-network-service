package cloud.fogbow.fns.api.http.request;

import cloud.fogbow.fns.api.http.response.ResourceId;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.api.http.response.FederatedNetworkInstance;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
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

    public static final String FEDERATED_NETWORK_ENDPOINT = SystemConstants.SERVICE_BASE_ENDPOINT + "federatedNetworks";

    @ApiOperation(value = ApiDocumentation.FederatedNetwork.CREATE_OPERATION)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ResourceId> createFederatedNetwork(
            @ApiParam(value = ApiDocumentation.FederatedNetwork.CREATE_REQUEST_BODY)
            @RequestBody cloud.fogbow.fns.api.parameters.FederatedNetwork federatedNetwork,
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Log.CREATE_FEDERATED_NETWORK_REQUEST_S, federatedNetwork.getOrder()));
            String federatedNetworkId = ApplicationFacade.getInstance().
                    createFederatedNetwork(federatedNetwork.getOrder(), systemUserToken);
            return new ResponseEntity<>(new ResourceId(federatedNetworkId), HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION_S, e.getMessage()));
            throw e;
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ApiOperation(value = ApiDocumentation.FederatedNetwork.GET_OPERATION)
    public ResponseEntity<Collection<InstanceStatus>> getFederatedNetworksStatus(
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws Exception {

        try {
            Collection<InstanceStatus> federatedNetworks = ApplicationFacade.getInstance().
                    getFederatedNetworksStatus(systemUserToken);
            return federatedNetworks == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(federatedNetworks);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION_S, e.getMessage()));
            throw e;
        }
    }

    @ApiOperation(value = ApiDocumentation.FederatedNetwork.GET_BY_ID_OPERATION)
    @GetMapping(value = "/{federatedNetworkId}")
    public ResponseEntity<FederatedNetworkInstance> getFederatedNetwork(
            @ApiParam(value = ApiDocumentation.FederatedNetwork.ID)
            @PathVariable String federatedNetworkId,
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Log.GET_FEDERATED_NETWORK_WITH_ID_S,
                    (federatedNetworkId == null ? "null" : federatedNetworkId)));
            FederatedNetworkOrder federatedNetwork = ApplicationFacade.getInstance().
                    getFederatedNetwork(federatedNetworkId, systemUserToken);
            FederatedNetworkInstance instance = federatedNetwork.getInstance();
            return new ResponseEntity<FederatedNetworkInstance>(instance, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION_S, e.getMessage()));
            return ResponseEntity.notFound().build();
        }
    }

    @ApiOperation(value = ApiDocumentation.FederatedNetwork.DELETE_OPERATION)
    @DeleteMapping(value = "/{federatedNetworkId}")
    public ResponseEntity<String> deleteFederatedNetwork(
            @ApiParam(value = ApiDocumentation.FederatedNetwork.ID)
            @PathVariable String federatedNetworkId,
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Log.DELETE_FEDERATED_NETWORK_S,
                    (federatedNetworkId == null ? "null" : federatedNetworkId)));
            ApplicationFacade.getInstance().deleteFederatedNetwork(federatedNetworkId, systemUserToken);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION_S, e.getMessage()));
            throw e;
        }
    }
}
