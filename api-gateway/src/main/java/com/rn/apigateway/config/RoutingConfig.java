package com.rn.apigateway.config;

import com.rn.apigateway.filter.SignInFilter;
import com.rn.apigateway.filter.SignUpFilter;
import com.rn.apigateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RoutingConfig {
    private final SignInFilter signInFilter;
    private final SignUpFilter signUpFilter;
    private final GatewayService gatewayService;
    public static final String AUTH_PATH = "/api/auth";
    public static final String USER_SERVICE_PATH = "/api/user";
    public static final String USER_SERVICE_URI = "http://user-service:8080";
    public static final String SSE_SERVICE_PATH = "/api/sse";
    public static final String SSE_SERVICE_URI = "http://sse-service:8081";
    //public static final String CHAT_SERVICE_PATH = "/api/chat";
    //public static final String CHAT_SERVICE_URI = "http://localhost:8081";


    public static class InnerHttpHeaders {
        public static final String AUTH_USER_ID =  "Auth-User-Id";
        public static final String AUTH_USER_ROLE =  "Auth-User-Role";
    }


    @Autowired
    public RoutingConfig(
        SignInFilter signInFilter,
        SignUpFilter signUpFilter,
        GatewayService gatewayService
    ) {
        this.signInFilter = signInFilter;
        this.signUpFilter = signUpFilter;
        this.gatewayService = gatewayService;
    }




    @Bean
    RouterFunction<ServerResponse> routes() {
        return route(POST(AUTH_PATH + "/signout"), gatewayService::signOut)
            .andRoute(POST(AUTH_PATH + "/refresh-token"), gatewayService::refreshToken);
    }

    @Bean
    public RouteLocator routing(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(r -> r
                .path(AUTH_PATH + "/signup")
                .filters(f -> f.setPath(USER_SERVICE_PATH + "/create"))
                .uri(USER_SERVICE_URI))
            .route(r -> r
                .path(AUTH_PATH + "/verify-email**")
                .filters(f -> f
                    .setPath(USER_SERVICE_PATH + "/verify-email")
                    .filter(signUpFilter))
                .uri(USER_SERVICE_URI))
            .route(r -> r
                .path(AUTH_PATH + "/signin")
                .filters(f -> f
                    .setPath(USER_SERVICE_PATH + "/authenticate")
                    .filter(signInFilter))
                .uri(USER_SERVICE_URI))
            .route(r -> r
                .path(AUTH_PATH + "/resend-email-verification")
                .filters(f -> f.setPath(USER_SERVICE_PATH + "/resend-email-verification"))
                .uri(USER_SERVICE_URI))
            .route(r -> r
                .path(AUTH_PATH + "/test")
                .filters(f -> f.setPath(USER_SERVICE_PATH + "/test"))
                .uri(USER_SERVICE_URI))
            .route(r -> r
                .path(AUTH_PATH + "/test/user")
                .filters(f -> f.setPath(USER_SERVICE_PATH + "/test/user"))
                .uri(USER_SERVICE_URI))
            .route(r -> r
                .path(AUTH_PATH + "/test/admin")
                .filters(f -> f.setPath(USER_SERVICE_PATH + "/test/admin"))
                .uri(USER_SERVICE_URI))
            .route(r -> r
                .path(SSE_SERVICE_PATH + "/subscribe")
                .uri(SSE_SERVICE_URI))
            /*.route(r -> r
                .path(CHAT_SERVICE_PATH_PATTERN)
                .uri(CHAT_SERVICE_URI))*/
            .build();
    }

}
