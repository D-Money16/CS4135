package elib.elib_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

@Configuration
public class GatewayConfig {

    @Value("${identity.service.url}")
    private String identityServiceUrl;

    @Value("${catalogue.service.url}")
    private String catalogueServiceUrl;

    @Value("${lending.service.url}")
    private String lendingServiceUrl;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    @Value("${bookclub.service.url}")
    private String bookclubServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> identityRoutes() {
        String url = identityServiceUrl;
        return GatewayRouterFunctions.route("identity-service")
            .route(RequestPredicates.path("/api/user/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(url));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> catalogueRoutes() {
        String url = catalogueServiceUrl;
        return GatewayRouterFunctions.route("catalogue-service")
            .route(RequestPredicates.path("/api/book-catalogue/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(url));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> lendingRoutes() {
        String url = lendingServiceUrl;
        return GatewayRouterFunctions.route("lending-service")
            .route(RequestPredicates.path("/api/lending/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(url));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationRoutes() {
        String url = notificationServiceUrl;
        return GatewayRouterFunctions.route("notification-service")
            .route(RequestPredicates.path("/api/notifications/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(url));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookclubRoutes() {
        String url = bookclubServiceUrl;
        return GatewayRouterFunctions.route("bookclub-service")
            .route(RequestPredicates.path("/api/bookclub/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(url));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }
}
