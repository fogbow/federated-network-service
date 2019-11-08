package cloud.fogbow.fns.api.http.request;

import cloud.fogbow.common.constants.ApiDocumentation;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.fns.constants.Messages;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = Version.VERSION_ENDPOINT)
@Api(description = ApiDocumentation.Version.API)
public class Version {

    public static final String VERSION_ENDPOINT = SystemConstants.SERVICE_BASE_ENDPOINT + "version";

    private final Logger LOGGER = Logger.getLogger(Version.class);

    @ApiOperation(value = ApiDocumentation.Version.GET_OPERATION)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<cloud.fogbow.fns.api.http.response.Version> getVersion() {
        try {
            String versionNumber = ApplicationFacade.getInstance().getVersionNumber();
            cloud.fogbow.fns.api.http.response.Version version = new cloud.fogbow.fns.api.http.response.Version(versionNumber);
            return new ResponseEntity<>(version, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }
}
