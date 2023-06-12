package com.rn.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final String accessTokenCookieName;
    private final ReactiveAuthenticationManager authenticationManager;




    @Autowired
    public SecurityContextRepository(
        @Value("${RedNet.app.accessTokenCookieName}") String accessTokenCookieName,
        ReactiveAuthenticationManager authenticationManager
    ) {
        this.accessTokenCookieName = accessTokenCookieName;
        this.authenticationManager = authenticationManager;
    }




    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie cookieAccessToken = cookies.getFirst(accessTokenCookieName);

        if (cookieAccessToken == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return Mono.empty();
        }

        String accessToken = cookieAccessToken.getValue();
        Authentication authentication = new UsernamePasswordAuthenticationToken(accessToken, null);

        return this.authenticationManager.authenticate(authentication).map(SecurityContextImpl::new);
    }
}
