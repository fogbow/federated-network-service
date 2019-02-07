package cloud.fogbow.fns.api.http;

import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.ras.api.http.CommonKeys;
import cloud.fogbow.ras.core.models.instances.ComputeInstance;
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
@Api(description = ApiDocumentation.Compute.API)
public class Compute {
    public static final String COMPUTE_ENDPOINT = "computes";

    private final Logger LOGGER = Logger.getLogger(Compute.class);

    @ApiOperation(value = ApiDocumentation.Compute.CREATE_OPERATION)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> createCompute(
            @ApiParam(value = ApiDocumentation.Compute.CREATE_REQUEST_BODY)
            @RequestBody cloud.fogbow.fns.api.parameters.Compute compute,
            @ApiParam(value = ApiDocumentation.CommonParameters.FEDERATION_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.CREATE_COMPUTE, compute));
            String computeId = ApplicationFacade.getInstance().createCompute(compute, federationTokenValue);
            return new ResponseEntity<String>(computeId, HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    @ApiOperation(value = ApiDocumentation.Compute.DELETE_OPERATION)
    @RequestMapping(value = "/{computeId}", method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteCompute(
            @ApiParam(value = ApiDocumentation.Compute.ID)
            @PathVariable String computeId,
            @ApiParam(value = ApiDocumentation.CommonParameters.FEDERATION_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.DELETE_COMPUTE, computeId));
            ApplicationFacade.getInstance().deleteCompute(computeId, federationTokenValue);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }

    @ApiOperation(value = ApiDocumentation.Compute.GET_BY_ID_OPERATION)
    @RequestMapping(value = "/{computeId}", method = RequestMethod.GET)
    public ResponseEntity<ComputeInstance> getCompute(
            @ApiParam(value = ApiDocumentation.Compute.ID)
            @PathVariable String computeId,
            @ApiParam(value = ApiDocumentation.CommonParameters.FEDERATION_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.FEDERATION_TOKEN_VALUE_HEADER_KEY) String federationTokenValue)
            throws Exception {

        try {
            LOGGER.info(String.format(Messages.Info.GET_COMPUTE_BY_ID, computeId));
            ComputeInstance compute = ApplicationFacade.getInstance().getComputeById(computeId, federationTokenValue);
            return new ResponseEntity<ComputeInstance>(compute, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }
}
