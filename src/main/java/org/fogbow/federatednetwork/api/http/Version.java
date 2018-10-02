package org.fogbow.federatednetwork.api.http;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.ApplicationFacade;
import org.fogbow.federatednetwork.constants.Messages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = Version.VERSION_ENDPOINT)
@Api(description = "Queries the version of the service's API.")
public class Version {

    public static final String VERSION_ENDPOINT = "version";

    private final Logger LOGGER = Logger.getLogger(Version.class);

    @ApiOperation(value = "Returns the version of the API.")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> getVersion() {
        LOGGER.info(Messages.Info.GET_VERSION_REQUEST_RECEIVED);
        String versionNumber = ApplicationFacade.getInstance().getVersionNumber();
        return new ResponseEntity<>(versionNumber, HttpStatus.OK);
    }
}
