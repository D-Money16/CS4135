package elib.elib_gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(ServiceInstanceUnavailableException.class)
    public ResponseEntity<GatewayErrorResponse> handleServiceUnavailable(ServiceInstanceUnavailableException ex, HttpServletRequest request) {
        return build(request, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<GatewayErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GatewayErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(request, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected gateway error");
    }

    private ResponseEntity<GatewayErrorResponse> build(HttpServletRequest request, HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new GatewayErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ));
    }
}