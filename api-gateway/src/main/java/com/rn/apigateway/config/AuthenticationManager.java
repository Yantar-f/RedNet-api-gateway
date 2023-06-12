package com.rn.apigateway.config;

import com.rn.apigateway.exception.ClaimNotPresentException;
import com.rn.apigateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;




    @Autowired
    public AuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }




    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String accessToken = authentication.getPrincipal().toString();

        if (jwtService.isTokenInvalid(accessToken)) {
            return Mono.empty();
        }

        try {
            Long id = Long.valueOf(jwtService.extractSubject(accessToken));
            List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(accessToken).stream()
                .map(SimpleGrantedAuthority::new).toList();

            Authentication newAuthentication = new UsernamePasswordAuthenticationToken(id, null, authorities);

            return Mono.just(newAuthentication);
        } catch (ClaimNotPresentException ex) {
            return Mono.empty();
        }
    }
}

