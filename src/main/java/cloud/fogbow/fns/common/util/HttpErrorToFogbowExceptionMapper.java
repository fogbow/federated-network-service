package cloud.fogbow.fns.common.util;

import cloud.fogbow.fns.common.exceptions.*;
import org.springframework.http.HttpStatus;

public class HttpErrorToFogbowExceptionMapper {
    public static FogbowException map(HttpStatus httpCode, String message) {
        switch(httpCode) {
            case FORBIDDEN:
                return new UnauthorizedRequestException(message);
            case UNAUTHORIZED:
                return new UnauthenticatedUserException(message);
            case BAD_REQUEST:
                return new InvalidParameterException(message);
            case NOT_FOUND:
                return new InstanceNotFoundException(message);
            case CONFLICT:
                return new QuotaExceededException(message);
            case NOT_ACCEPTABLE:
                return new NoAvailableResourcesException(message);
            case GATEWAY_TIMEOUT:
                return new UnavailableProviderException(message);
            case INTERNAL_SERVER_ERROR:
            case UNSUPPORTED_MEDIA_TYPE:
            default:
                return new UnexpectedException(message);
        }
    }
}
