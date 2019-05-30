package cloud.fogbow.fns.api.http.request;

import cloud.fogbow.common.exceptions.*;
import cloud.fogbow.ras.api.http.request.*;
import org.apache.log4j.Logger;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.utils.RedirectUtil;
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

    @RequestMapping(value = {   "/" + Attachment.ATTACHMENT_ENDPOINT + "/**",
                                "/" + Cloud.CLOUD_ENDPOINT + "/**",
                                "/" + Compute.COMPUTE_ENDPOINT + "/" + cloud.fogbow.ras.api.http.request.Compute.STATUS_SUFFIX_ENDPOINT,
                                "/" + Compute.COMPUTE_ENDPOINT + "/" + cloud.fogbow.ras.api.http.request.Compute.QUOTA_SUFFIX_ENDPOINT + "/**",
                                "/" + Compute.COMPUTE_ENDPOINT + "/" + cloud.fogbow.ras.api.http.request.Compute.ALLOCATION_SUFFIX_ENDPOINT + "/**",
                                "/" + GenericRequest.GENERIC_REQUEST_ENDPOINT + "/**",
                                "/" + Image.IMAGE_ENDPOINT + "/**",
                                "/" + Network.NETWORK_ENDPOINT + "/**",
                                "/" + PublicIp.PUBLIC_IP_ENDPOINT + "/**",
                                "/" + Volume.VOLUME_ENDPOINT + "/**"})
    public ResponseEntity redirectRequest(@RequestBody(required = false) String body, HttpMethod method,
                                     HttpServletRequest request) throws URISyntaxException, FatalErrorException,
            FogbowException {

        try {
            LOGGER.info(Messages.Info.GENERIC_REQUEST);
            return RedirectUtil.redirectRequest(body, method, request, String.class);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }
}
