package elib.elib_gateway;

import java.time.Instant;

public record GatewayErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}