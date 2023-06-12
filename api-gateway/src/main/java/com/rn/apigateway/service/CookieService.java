package com.rn.apigateway.service;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.concurrent.TimeUnit;

@Service
public class CookieService {
    private final String accessTokenCookieName;
    private final String accessTokenCookiePath;
    private final String refreshTokenCookieName;
    private final String refreshTokenCookiePath;
    private final Long accessCookieExpiration;
    private final Long refreshCookieExpiration;

    public CookieService(
        @Value ("${RedNet.app.accessTokenCookieName}") String accessTokenCookieName,
        @Value ("${RedNet.app.accessTokenCookiePath}") String accessTokenCookiePath,
        @Value ("${RedNet.app.refreshTokenCookieName}")String refreshTokenCookieName,
        @Value ("${RedNet.app.refreshTokenCookiePath}")String refreshTokenCookiePath,
        @Value ("${RedNet.app.accessTokenCookieExpirationMs}") Long accessCookieExpirationMs,
        @Value ("${RedNet.app.refreshTokenCookieExpirationMs}") Long refreshCookieExpirationMs
    ) {
        this.accessTokenCookieName = accessTokenCookieName;
        this.accessTokenCookiePath = accessTokenCookiePath;
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.refreshTokenCookiePath = refreshTokenCookiePath;
        this.accessCookieExpiration = TimeUnit.MILLISECONDS.toSeconds(accessCookieExpirationMs);
        this.refreshCookieExpiration = TimeUnit.MILLISECONDS.toSeconds(refreshCookieExpirationMs);
    }

    public ResponseCookie generateAccessTokenCookie(String value) {
        return ResponseCookie.from(accessTokenCookieName, value)
            .path(accessTokenCookiePath)
            .maxAge(accessCookieExpiration)
            .httpOnly(true)
            .build();
    }

    public ResponseCookie generateRefreshTokenCookie(String value) {
        return ResponseCookie.from(refreshTokenCookieName, value)
            .path(refreshTokenCookiePath)
            .maxAge(refreshCookieExpiration)
            .httpOnly(true)
            .build();
    }

    public ResponseCookie generateAccessTokenCleaningCookie() {
        return ResponseCookie.from(accessTokenCookieName)
            .path(accessTokenCookiePath)
            .maxAge(0)
            .httpOnly(true)
            .build();
    }

    public ResponseCookie generateRefreshTokenCleaningCookie() {
        return ResponseCookie.from(refreshTokenCookieName)
            .path(refreshTokenCookiePath)
            .maxAge(0)
            .httpOnly(true)
            .build();
    }

    @Nullable
    public HttpCookie extractRefreshTokenCookie(ServerRequest request) {
        return request.cookies().getFirst(refreshTokenCookieName);
    }
}
