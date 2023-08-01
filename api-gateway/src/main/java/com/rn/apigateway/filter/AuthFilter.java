package com.rn.apigateway.filter;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter {
    String apiTokenCookieName;
    String apiTokenCookiePath;
    long apiTokenExpirationMs;
    long apiTokenCookieExpirationS;
    String authTokenCookieName;
    String authTokenCookiePath;
    long authTokenExpirationMs;
    long authTokenCookieExpirationS;
    JwtParser authTokenParserBuilder;
    JwtParser apiTokenParserBuilder;




    @Autowired
    public AuthFilter(
        @Value("${rednet.app.api-token-cookie-name}") String apiTokenCookieName,
        @Value("${rednet.app.api-token-cookie-path}") String apiTokenCookiePath,
        @Value("${rednet.app.api-token-expiration-ms}") long apiTokenExpirationMs,
        @Value("${rednet.app.api-token-cookie-expiration-s}") long apiTokenCookieExpirationS,
        @Value("${rednet.app.api-token-issuer}") String apiTokenIssuer,
        @Value("${rednet.app.api-token-secret-key}") String apiTokenSecretKey,
        @Value("${rednet.app.auth-token-cookie-name}") String authTokenCookieName,
        @Value("${rednet.app.auth-token-cookie-path}") String authTokenCookiePath,
        @Value("${rednet.app.auth-token-expiration-ms}") long authTokenExpirationMs,
        @Value("${rednet.app.auth-token-cookie-expiration-s}") long authTokenCookieExpirationS,
        @Value("${rednet.app.auth-token-issuer}") String authTokenIssuer,
        @Value("${rednet.app.auth-token-secret-key}") String authTokenSecretKey
    ) {
        this.apiTokenCookieName = apiTokenCookieName;
        this.apiTokenCookiePath = apiTokenCookiePath;
        this.apiTokenExpirationMs = apiTokenExpirationMs;
        this.apiTokenCookieExpirationS = apiTokenCookieExpirationS;
        this.authTokenCookieName = authTokenCookieName;
        this.authTokenCookiePath = authTokenCookiePath;
        this.authTokenExpirationMs = authTokenExpirationMs;
        this.authTokenCookieExpirationS = authTokenCookieExpirationS;

        apiTokenParserBuilder = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(apiTokenSecretKey)))
            .requireIssuer(apiTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();

        authTokenParserBuilder = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(authTokenSecretKey)))
            .requireIssuer(authTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();
    }




    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ///
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ///
        }));
    }
}
