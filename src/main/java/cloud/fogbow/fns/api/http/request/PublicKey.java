package cloud.fogbow.fns.api.http.request;

import cloud.fogbow.common.constants.ApiDocumentation;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.fns.core.ApplicationFacade;
import cloud.fogbow.common.exceptions.UnexpectedException;
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
@RequestMapping(value = PublicKey.PUBLIC_KEY_ENDPOINT)
@Api(description = ApiDocumentation.PublicKey.API)
public class PublicKey {
    public static final String PUBLIC_KEY_ENDPOINT = SystemConstants.SERVICE_BASE_ENDPOINT + "publicKey";

    private final Logger LOGGER = Logger.getLogger(PublicKey.class);

    @ApiOperation(value = ApiDocumentation.PublicKey.GET_OPERATION)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<cloud.fogbow.fns.api.http.response.PublicKey> getPublicKey() throws UnexpectedException {
        try {
            LOGGER.info(Messages.Info.GET_PUBLIC_KEY);
            String publicKeyValue = ApplicationFacade.getInstance().getPublicKey();
            cloud.fogbow.fns.api.http.response.PublicKey publicKey = new cloud.fogbow.fns.api.http.response.PublicKey(publicKeyValue);
            return new ResponseEntity<>(publicKey, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()), e);
            throw e;
        }
    }
}
