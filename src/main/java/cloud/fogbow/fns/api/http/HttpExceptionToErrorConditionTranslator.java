package cloud.fogbow.fns.api.http;

import cloud.fogbow.common.exceptions.*;
import cloud.fogbow.ras.api.http.ExceptionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class HttpExceptionToErrorConditionTranslator extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnauthorizedRequestException.class)
    public final ResponseEntity<ExceptionResponse> handleAuthorizationException(Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthenticatedUserException.class)
    public final ResponseEntity<ExceptionResponse> handleAuthenticationException(Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public final ResponseEntity<ExceptionResponse> handleInvalidParameterException(Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InstanceNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleInstanceNotFoundException(Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConfigurationErrorException.class)
    public final ResponseEntity<ExceptionResponse> handleQuotaExceededException(Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnacceptableOperationException.class)
    public final ResponseEntity<ExceptionResponse> handleNoAvailableResourcesException(
            Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler({UnavailableProviderException.class})
    public final ResponseEntity<ExceptionResponse> handleUnavailableProviderException(
            Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(UnexpectedException.class)
    public final ResponseEntity<ExceptionResponse> handleUnexpectedException(Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*
    It should never happen because any Exception must be mapped to one of the above FogbowException extensions.
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAnyException(Exception ex, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    public final ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        ExceptionResponse errorDetails = new ExceptionResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }
}
