package org.fogbow.federatednetwork.api.http;

import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.exceptions.FederatedNetworkNotFoundException;
import org.fogbow.federatednetwork.exceptions.NotEmptyFederatedNetworkException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.ConnectException;

@ControllerAdvice
public class HttpExceptionToErrorConditionTranslator {

    @ResponseStatus(value=HttpStatus.BAD_GATEWAY, reason= Messages.Error.RESOURCE_ALLOCATION_SERVICE_DOES_NOT_RESPOND)
    @ExceptionHandler(ConnectException.class)
    public final void handleConnectException() {
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason= Messages.Error.UNABLE_TO_FIND_FEDERATED_NETWORK)
    @ExceptionHandler(FederatedNetworkNotFoundException.class)
    public final void handleFederatedNetworkNotFoundException() {
    }

    @ResponseStatus(value=HttpStatus.PRECONDITION_FAILED, reason= Messages.Error.FEDERATED_NETWORK_IN_USE)
    @ExceptionHandler(NotEmptyFederatedNetworkException.class)
    public final void handleNotEmptyFederatedNetworkException() {
    }
}
