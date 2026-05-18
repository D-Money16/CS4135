package elib.elib_gateway;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
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

    private final LoadBalancerClient loadBalancer;

    public GatewayConfig(LoadBalancerClient loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    private URI resolve(String serviceId) {
        ServiceInstance instance = loadBalancer.choose(serviceId);
        if (instance == null) {
            throw new ServiceInstanceUnavailableException(serviceId);
        }
        return instance.getUri();
    }

    @Bean
    public RouterFunction<ServerResponse> identityRoutes() {
        return GatewayRouterFunctions.route("identity-service")
            .route(RequestPredicates.path("/api/user/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, resolve("elib-identity"));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> catalogueRoutes() {
        return GatewayRouterFunctions.route("catalogue-service")
            .route(RequestPredicates.path("/api/book-catalogue/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, resolve("elib-catalogue"));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> lendingRoutes() {
        return GatewayRouterFunctions.route("lending-service")
            .route(RequestPredicates.path("/api/lending/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, resolve("elib-lending"));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationRoutes() {
        return GatewayRouterFunctions.route("notification-service")
            .route(RequestPredicates.path("/api/notifications/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, resolve("elib-notification"));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookclubRoutes() {
        return GatewayRouterFunctions.route("bookclub-service")
            .route(RequestPredicates.path("/api/bookclub/**"), request -> {
                request.attributes().put(MvcUtils.GATEWAY_REQUEST_URL_ATTR, resolve("elib-bookclub"));
                return HandlerFunctions.http().handle(request);
            })
            .build();
    }
}
