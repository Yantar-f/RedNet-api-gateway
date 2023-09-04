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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component
public class AuthFilter implements Ordered, GlobalFilter {
    private final String apiTokenCookieName;
    private final long apiTokenExpirationMs;
    private final String authTokenCookieName;
    private final JwtParser authTokenParser;
    private final String apiTokenIssuer;
    private final String apiTokenSecretKey;


    @Autowired
    public AuthFilter(
        @Value("${rednet.app.api-token-cookie-name}") String apiTokenCookieName,
        @Value("${rednet.app.api-token-expiration-ms}") long apiTokenExpirationMs,
        @Value("${rednet.app.api-token-issuer}") String apiTokenIssuer,
        @Value("${rednet.app.api-token-secret-key}") String apiTokenSecretKey,
        @Value("${rednet.app.access-token-cookie-name}") String authTokenCookieName,
        @Value("${rednet.app.auth-token-issuer}") String authTokenIssuer,
        @Value("${rednet.app.access-token-secret-key}") String authTokenSecretKey
    ) {
        this.apiTokenCookieName = apiTokenCookieName;
        this.apiTokenExpirationMs = apiTokenExpirationMs;
        this.authTokenCookieName = authTokenCookieName;
        this.apiTokenIssuer = apiTokenIssuer;
        this.apiTokenSecretKey = apiTokenSecretKey;

        authTokenParser = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(authTokenSecretKey)))
            .requireIssuer(authTokenIssuer)
            .setAllowedClockSkewSeconds(5)
            .build();
    }




    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().headers((httpHeaders) -> {
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

            String apiTokenCookie = new HttpCookie(apiTokenCookieName, apiTokenBuilder()
                .setSubject(userID)
                .claim("roles", roles)
                .setExpiration(new Date(System.currentTimeMillis() + apiTokenExpirationMs))
                .compact()).toString();

            httpHeaders.set("Cookie", apiTokenCookie);
        }).build()).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private JwtBuilder apiTokenBuilder() {
        return Jwts.builder()
                .setIssuer(apiTokenIssuer)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(apiTokenSecretKey)), SignatureAlgorithm.HS256);
    }
}
