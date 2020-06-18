package cloud.fogbow.fns.api.http.request;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.api.http.response.ServiceList;
import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.ras.api.http.CommonKeys;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = Service.SERVICE_ENDPOINT)
@Api(description = ApiDocumentation.Service.API)
public class Service {
    public static final String SERVICE_SUFFIX_ENDPOINT = "services";
    public static final String SERVICE_ENDPOINT = SystemConstants.SERVICE_BASE_ENDPOINT + SERVICE_SUFFIX_ENDPOINT;

    private final Logger LOGGER = Logger.getLogger(Service.class);

    @ApiOperation(value = ApiDocumentation.Service.GET_OPERATION)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ServiceList> getServiceNames(
            @ApiParam(value = cloud.fogbow.common.constants.ApiDocumentation.Token.SYSTEM_USER_TOKEN)
            @RequestHeader(required = false, value = CommonKeys.SYSTEM_USER_TOKEN_HEADER_KEY) String systemUserToken)
            throws FogbowException {
        try {
            LOGGER.debug(Messages.Log.RECEIVING_GET_SERVICES_REQUEST);
            List<String> serviceNames = ApplicationFacade.getInstance().getServiceNames(systemUserToken);
            return new ResponseEntity<>(new ServiceList(serviceNames), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION_S, e.getMessage()), e);
            throw e;
        }
    }
}
