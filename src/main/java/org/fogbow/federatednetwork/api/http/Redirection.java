package org.fogbow.federatednetwork.api.http;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.exceptions.FatalErrorException;
import org.fogbow.federatednetwork.utils.HttpUtil;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;

import org.fogbowcloud.ras.api.http.*;
import org.fogbowcloud.ras.api.http.Compute;

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
                                "/" + Compute.COMPUTE_ENDPOINT + "/" + Compute.STATUS_ENDPOINT,
                                "/" + Compute.COMPUTE_ENDPOINT + "/" + Compute.QUOTA_ENDPOINT + "/**",
                                "/" + Compute.COMPUTE_ENDPOINT + "/" + Compute.ALLOCATION_ENDPOINT + "/**",
                                "/" + GenericRequest.GENERIC_REQUEST_ENDPOINT + "/**",
                                "/" + Image.IMAGE_ENDPOINT + "/**",
                                "/" + Network.NETWORK_ENDPOINT + "/**",
                                "/" + PublicIp.PUBLIC_IP_ENDPOINT + "/**",
                                "/" + Token.TOKEN_ENDPOINT + "/**",
                                "/" + Volume.VOLUME_ENDPOINT + "/**"})
    public ResponseEntity redirectRequest(@RequestBody(required = false) String body, HttpMethod method,
                                     HttpServletRequest request) throws URISyntaxException, FatalErrorException {

        try {
            LOGGER.info(Messages.Info.GENERIC_REQUEST);
            return HttpUtil.redirectRequest(body, method, request, String.class);
        } catch (Exception e) {
            LOGGER.info(String.format(Messages.Exception.GENERIC_EXCEPTION, e.getMessage()));
            throw e;
        }
    }
}
