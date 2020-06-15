package cloud.fogbow.fns.api.http;

import cloud.fogbow.common.http.FogbowExceptionToHttpErrorConditionTranslator;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class FnsExceptionToHttpErrorConditionTranslator extends FogbowExceptionToHttpErrorConditionTranslator {
}
