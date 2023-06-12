package com.rn.apigateway.service;

import com.rn.apigateway.payload.AuthenticationBody;
import com.rn.apigateway.repository.RefreshTokenRepository;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class GatewayService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;




    public GatewayService(
        RefreshTokenRepository refreshTokenRepository,
        JwtService jwtService,
        CookieService cookieService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.cookieService = cookieService;
    }




    public Mono<ServerResponse> signOut(ServerRequest request) {
        HttpCookie oldRefreshTokenCookie = cookieService.extractRefreshTokenCookie(request);

        if (oldRefreshTokenCookie == null) {
            return ServerResponse.badRequest().build();
        } else if (jwtService.isTokenInvalid(oldRefreshTokenCookie.getValue())) {
            return ServerResponse.status(HttpStatus.FORBIDDEN.value()).build();
        }

        String userId = jwtService.extractSubject(oldRefreshTokenCookie.getValue());
        return refreshTokenRepository.deleteById(userId).flatMap(bool ->
            ServerResponse.ok()
                .cookie(cookieService.generateAccessTokenCleaningCookie())
                .cookie(cookieService.generateRefreshTokenCleaningCookie())
                .build());
    }

    public Mono<ServerResponse> refreshToken(ServerRequest request) {
        HttpCookie oldRefreshTokenCookie = cookieService.extractRefreshTokenCookie(request);

        if (oldRefreshTokenCookie == null) {
            return ServerResponse.badRequest().build();
        } else if (jwtService.isTokenInvalid(oldRefreshTokenCookie.getValue())) {
            return ServerResponse.status(HttpStatus.FORBIDDEN.value()).build();
        }

        String userId = jwtService.extractSubject(oldRefreshTokenCookie.getValue());
        return refreshTokenRepository.deleteById(userId)
            .flatMap(isPresent -> {
                if (isPresent) {
                    AuthenticationBody authenticationBody = new AuthenticationBody(
                        jwtService.extractSubject(userId),
                        jwtService.extractRoles(oldRefreshTokenCookie.getValue())
                    );

                    String newAccessToken = jwtService.generateAccessToken(authenticationBody);
                    String newRefreshToken = jwtService.generateRefreshToken(authenticationBody);
                    return refreshTokenRepository.getAndSave(userId,newRefreshToken)
                        .flatMap(token -> ServerResponse.ok()
                            .cookie(cookieService.generateAccessTokenCookie(newAccessToken))
                            .cookie(cookieService.generateRefreshTokenCookie(newRefreshToken))
                            .build());
                } else {
                    return ServerResponse.status(HttpStatus.FORBIDDEN.value()).build();
                }
            });
    }
}
