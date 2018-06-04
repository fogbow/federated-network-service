package org.fogbow.federatednetwork.api.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = FederatedComputeRestHandler.COMPUTE_ENDPOINT)
public class FederatedComputeRestHandler {

	public static final String COMPUTE_ENDPOINT = "computes";

}
