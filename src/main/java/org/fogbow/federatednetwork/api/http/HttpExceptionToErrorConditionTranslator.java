package org.fogbow.federatednetwork.api.http;

import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.ConnectException;

@ControllerAdvice
public class HttpExceptionToErrorConditionTranslator {

    public static final String RESOURCE_ALLOCATION_SERVICE_COULD_NOT_RESPOND = "Resource Allocation Service could not respond.";
    @ResponseStatus(value=HttpStatus.BAD_GATEWAY,
            reason= RESOURCE_ALLOCATION_SERVICE_COULD_NOT_RESPOND)
    @ExceptionHandler(ConnectException.class)
    public final void handleConnectException() {
    }

    public static final String FEDERATED_NETWORK_NOT_FOUND = "Federated Network not found.";
    @ResponseStatus(value=HttpStatus.NOT_FOUND,
            reason= FEDERATED_NETWORK_NOT_FOUND)
    @ExceptionHandler(FederatedNetworkNotFoundException.class)
    public final void handleFederatedNetworkNotFoundException() {
    }
}
