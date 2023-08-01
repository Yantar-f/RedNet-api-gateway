package com.rn.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfig {



    @Autowired
    public RoutingConfig(

    ) {

    }

    @Bean
    public RouteLocator routing(RouteLocatorBuilder builder) {
        return builder.routes()
            .route(r -> r
                .path("/res")
                .filters(f -> f.setPath("/res1"))
                .uri("http://localhost:8001"))
            /*
            .route(r -> r
                .path("AUTH_PATH" + "/verify-email**")
                .filters(f -> f
                    .setPath(USER_SERVICE_PATH + "/verify-email")
                    .filter(signUpFilter))
                .uri(USER_SERVICE_URI))*/
            .build();
    }

}
