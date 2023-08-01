package com.rn.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfig {
    public static final String AUTH_PATH = "/api/auth";
    public static final String USER_SERVICE_PATH = "/api/user";
    public static final String USER_SERVICE_URI = "http://user-service:8080";
    public static final String SSE_SERVICE_PATH = "/api/sse";
    public static final String SSE_SERVICE_URI = "http://sse-service:8081";



    @Autowired
    public RoutingConfig(

    ) {

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
            .build();
    }

}
