package com.rn.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfig {

    public RoutingConfig() {
    }

    @Bean
    public RouteLocator routing(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(r -> r
                .path(
                    "/api/auth/signup",
                    "/api/auth/signin",
                    "/api/auth/signout",
                    "/api/auth/refresh-tokens",
                    "/api/auth/verify-email",
                    "/api/auth/resend-email-verification",
                    "/api/auth/change-password")
                .filters(f -> f.stripPrefix(2))
                .uri("http://localhost:8001"))
            .build();
    }

}
