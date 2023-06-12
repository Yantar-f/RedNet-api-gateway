package com.rn.apigateway.filter;

import com.rn.apigateway.payload.AuthenticationBody;
import com.rn.apigateway.repository.RefreshTokenRepository;
import com.rn.apigateway.service.CookieService;
import com.rn.apigateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.rn.apigateway.config.RoutingConfig.InnerHttpHeaders.AUTH_USER_ID;
import static com.rn.apigateway.config.RoutingConfig.InnerHttpHeaders.AUTH_USER_ROLE;

@Component
public class JwtSignUpFilter extends SignUpFilter {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;





    @Autowired
    public JwtSignUpFilter(
        JwtService jwtService,
        CookieService cookieService,
        RefreshTokenRepository refreshTokenRepository
    ) {
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.refreshTokenRepository = refreshTokenRepository;
    }





    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            if (response.getStatusCode().is2xxSuccessful()){
                AuthenticationBody authenticationBody = new AuthenticationBody(
                    response.getHeaders().getFirst(AUTH_USER_ID),
                    response.getHeaders().get(AUTH_USER_ROLE)
                );

                response.addCookie(cookieService.generateAccessTokenCookie(
                    jwtService.generateAccessToken(authenticationBody))
                );

                String refreshToken = jwtService.generateRefreshToken(authenticationBody);
                response.addCookie(cookieService.generateRefreshTokenCookie(refreshToken));

                response.getHeaders().remove(AUTH_USER_ID);
                response.getHeaders().remove(AUTH_USER_ROLE);
                refreshTokenRepository.save(authenticationBody.getId(),refreshToken);
            }
        }));
    }
}
