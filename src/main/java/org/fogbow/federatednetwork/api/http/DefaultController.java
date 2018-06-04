package org.fogbow.federatednetwork.api.http;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class DefaultController {

	// TODO: this should be retrieved from a configuration file.
	private final String forwardFogbowUrl = "forward:localhost:8080/%s";

	@RequestMapping(value = "/**/{[path:[^\\.]*}")
	public ModelAndView method(final HttpServletRequest request) {
		final String url = request.getRequestURI();
		String fogbowEndpoint = "";

		if (url.startsWith("/static")) {
			fogbowEndpoint = String.format(forwardFogbowUrl, url);
		}
		return new ModelAndView(fogbowEndpoint);

	}
}
