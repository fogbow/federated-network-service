package org.fogbow.federatednetwork.api.http;

import org.fogbowcloud.manager.api.http.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class ExceptionToResponseEntityTranslator {

    @ExceptionHandler(HttpClientErrorException.class)
    public final ResponseEntity<String> handleThrowable(HttpClientErrorException exception) {
        exception.getMessage();
        return new ResponseEntity<String>("testando", HttpStatus.FORBIDDEN);
    }

}
