package org.fogbow.federatednetwork.api.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = FederatedComputeController.COMPUTE_ENDPOINT)
public class FederatedComputeController {

	public static final String COMPUTE_ENDPOINT = "compute";

}
