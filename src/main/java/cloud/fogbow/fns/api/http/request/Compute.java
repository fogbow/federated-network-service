package cloud.fogbow.fns.api.http.request;

import cloud.fogbow.fns.api.http.response.ResourceId;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.ras.api.http.CommonKeys;
import cloud.fogbow.ras.api.http.response.ComputeInstance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.constants.Messages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(value = Compute.COMPUTE_ENDPOINT)
@Api(description = cloud.fogbow.ras.constants.ApiDocumentation.Compute.API)
public class Compute {
    public static final String COMPUTE_ENDPOINT = SystemConstants.SERVICE_BASE_ENDPOINT +
            cloud.fogbow.ras.api.http.request.Compute.COMPUTE_SUFFIX_ENDPOINT;

    private final Logger LOGGER = Logger.getLogger(Compute.class);

    @ApiOperation(value = cloud.fogbow.ras.constants.ApiDocumentation.Compute.CREATE_OPERATION)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ResourceId> createCompute(
            @ApiParam(value = ApiDocumentation.Compute.CREATE_REQUEST_BODY)
            @RequestBody FederatedCompute federatedCompute,
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.CREATE_COMPUTE, federatedCompute));
            String computeId = ApplicationFacade.getInstance().createCompute(federatedCompute, systemUserToken);
            return new ResponseEntity<ResourceId>(new ResourceId(computeId), HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @ApiOperation(value = cloud.fogbow.ras.constants.ApiDocumentation.Compute.DELETE_OPERATION)
    @RequestMapping(value = "/{computeId}", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteCompute(
            @ApiParam(value = cloud.fogbow.ras.constants.ApiDocumentation.Compute.ID)
            @PathVariable String computeId,
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.DELETE_COMPUTE, computeId));
            ApplicationFacade.getInstance().deleteCompute(computeId, systemUserToken);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @ApiOperation(value = cloud.fogbow.ras.constants.ApiDocumentation.Compute.GET_BY_ID_OPERATION)
    @RequestMapping(value = "/{computeId}", method = RequestMethod.GET)
    public ResponseEntity<ComputeInstance> getCompute(
            @ApiParam(value = cloud.fogbow.ras.constants.ApiDocumentation.Compute.ID)
            @PathVariable String computeId,
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.GET_COMPUTE_BY_ID, computeId));
            ComputeInstance compute = ApplicationFacade.getInstance().getComputeById(computeId, systemUserToken);
            return new ResponseEntity<ComputeInstance>(compute, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
