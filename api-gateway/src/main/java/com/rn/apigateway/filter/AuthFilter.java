package com.rn.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;

@Component
public class AuthFilter implements Ordered, GlobalFilter {
    private final String apiTokenCookieName;
    private final String apiTokenCookiePath;
    private final long apiTokenExpirationMs;
    private final long apiTokenCookieExpirationS;
    private final String authTokenCookieName;
    private final String authTokenCookiePath;
    private final long authTokenExpirationMs;
    private final long authTokenCookieExpirationS;
    private final JwtParser authTokenParser;
    private final JwtParser apiTokenParser;
    private final JwtBuilder authTokenBuilder;
    private final JwtBuilder apiTokenBuilder;



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

        apiTokenParser = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(apiTokenSecretKey)))
            .requireIssuer(apiTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();

        authTokenParser = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(authTokenSecretKey)))
            .requireIssuer(authTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();

        apiTokenBuilder = Jwts.builder()
            .setIssuer(apiTokenIssuer)
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(apiTokenSecretKey)), SignatureAlgorithm.HS256);

        authTokenBuilder = Jwts.builder()
            .setIssuer(authTokenIssuer)
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(authTokenSecretKey)), SignatureAlgorithm.HS256);
    }




    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest().mutate().headers((httpHeaders) -> {
            String userID = "service-guest";
            String[] roles = new String[]{"ROLE_GUEST"};
            HttpCookie authTokenCookie = exchange.getRequest().getCookies().getFirst(authTokenCookieName);

            if (authTokenCookie != null) {
                String authToken = authTokenCookie.getValue();

                try {
                    Claims claims = authTokenParser.parseClaimsJws(authToken).getBody();
                    userID = claims.getSubject();
                    roles = (String[]) claims.get("roles");
                } catch (
                    SignatureException |
                    MalformedJwtException |
                    ExpiredJwtException |
                    UnsupportedJwtException |
                    IllegalArgumentException e
                ) {
                    ///
                    System.out.println("Invalid token");
                    ///
                }
            }

            String apiTokenCookie = new HttpCookie(apiTokenCookieName, apiTokenBuilder
                .setSubject(userID)
                .claim("roles", roles)
                .setExpiration(new Date(System.currentTimeMillis() + apiTokenExpirationMs))
                .compact()).toString();

            httpHeaders.set("Cookie", apiTokenCookie);
        }).build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
