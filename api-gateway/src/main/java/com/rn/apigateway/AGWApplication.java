package com.rn.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AGWApplication {

    public static void main(String[] args) {
        SpringApplication.run(AGWApplication.class, args);
    }

}


