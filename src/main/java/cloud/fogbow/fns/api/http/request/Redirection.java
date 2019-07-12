package cloud.fogbow.fns.api.http.request;

import cloud.fogbow.common.exceptions.*;
import cloud.fogbow.fns.constants.SystemConstants;
import cloud.fogbow.ras.api.http.request.*;
import cloud.fogbow.ras.api.http.request.Compute;
import org.apache.log4j.Logger;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.utils.RedirectToRasUtil;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS,
        RequestMethod.PUT})
@Controller
@ApiIgnore
public class Redirection {
    private static final Logger LOGGER = Logger.getLogger(Redirection.class);

    @RequestMapping(value = {   "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Attachment.ATTACHMENT_SUFFIX_ENDPOINT + "/**",
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Cloud.CLOUD_SUFFIX_ENDPOINT + "/**",
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Compute.COMPUTE_SUFFIX_ENDPOINT + "/" + Compute.STATUS_SUFFIX_ENDPOINT,
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Compute.COMPUTE_SUFFIX_ENDPOINT + "/" + Compute.QUOTA_SUFFIX_ENDPOINT + "/**",
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Compute.COMPUTE_SUFFIX_ENDPOINT + "/" + Compute.ALLOCATION_SUFFIX_ENDPOINT + "/**",
//                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + GenericRequest.GENERIC_REQUEST_SUFFIX_ENDPOINT + "/**",
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Image.IMAGE_SUFFIX_ENDPOINT + "/**",
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Network.NETWORK_SUFFIX_ENDPOINT + "/**",
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + PublicIp.PUBLIC_IP_SUFFIX_ENDPOINT + "/**",
                                "/" + SystemConstants.SERVICE_BASE_ENDPOINT + Volume.VOLUME_SUFFIX_ENDPOINT + "/**"})
    public ResponseEntity redirectRequest(@RequestBody(required = false) String body, HttpMethod method,
                                     HttpServletRequest request) throws URISyntaxException, FatalErrorException,
            FogbowException {

        try {
            LOGGER.info(Messages.Info.REDIRECT_REQUEST);
            return RedirectToRasUtil.redirectRequestToRas(body, method, request, String.class);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }
}
