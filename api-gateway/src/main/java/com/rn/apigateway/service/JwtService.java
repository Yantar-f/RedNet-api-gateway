package com.rn.apigateway.service;

import com.rn.apigateway.exception.ClaimNotPresentException;
import com.rn.apigateway.payload.AuthenticationBody;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtService {
    private final Integer accessTokenExpirationMs;
    private final Integer refreshTokenExpirationMs;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private final Key signingKey;
    private final JwtParser jwtParser;




    public JwtService(
        @Value ("${RedNet.app.jwt.secretKey}") String secretKey,
        @Value("${RedNet.app.accessTokenExpirationMs}") Integer accessTokenExpirationMs,
        @Value("${RedNet.app.refreshTokenExpirationMs}") Integer refreshTokenExpirationMs
    ) {
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();
    }




    public String generateAccessToken(AuthenticationBody authenticationBody) {
        HashMap<String,Object> rolesClaims = new HashMap<>();
        rolesClaims.put(
            "roles",
            authenticationBody.getRoles()
        );
        return generateAccessToken(rolesClaims, authenticationBody);
    }

    public String generateAccessToken(
        Map<String, Object> extraClaims,
        AuthenticationBody user
    ) {
        return getInitialBuilder()
            .setClaims(extraClaims)
            .setSubject(user.getId())
            .setExpiration(new Date(System.currentTimeMillis() + getAccessTokenExpirationMs()))
            .compact();
    }

    public String generateRefreshToken(AuthenticationBody authenticationBody) {
        HashMap<String,Object> rolesClaims = new HashMap<>();
        rolesClaims.put(
            "roles",
            authenticationBody.getRoles()
        );
        return generateRefreshToken(rolesClaims, authenticationBody);
    }

    public String generateRefreshToken(
        Map<String, Object> extraClaims,
        AuthenticationBody authenticationBody
    ) {
        return getInitialBuilder()
            .setClaims(extraClaims)
            .setSubject(authenticationBody.getId())
            .setExpiration(new Date(System.currentTimeMillis() + getRefreshTokenExpirationMs()))
            .compact();
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        try {
            return (List<String>) extractClaim(token, claims -> claims.get("roles"));
        } catch (ClassCastException ex) {
            throw new ClaimNotPresentException("roles");
        }

    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return Optional
            .ofNullable(claimsResolver.apply(claims))
            .orElseThrow(ClaimNotPresentException::new);
    }

    public Claims extractAllClaims(String token) {
        return getJwtParser()
            .parseClaimsJws(token)
            .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            getJwtParser().parseClaimsJws(token);
            return true;
        } catch (
            UnsupportedJwtException |
            MalformedJwtException |
            SecurityException |
            ExpiredJwtException |
            IllegalArgumentException e
        ) {
            return false;
        }
    }

    public boolean isTokenInvalid(String token) {
        return isTokenValid(token);
    }




    private JwtBuilder getInitialBuilder(){
        return Jwts
            .builder()
            .signWith(getSigningKey(), getSignatureAlgorithm())
            .setIssuedAt(new Date(System.currentTimeMillis()));
    }

    private JwtParser getJwtParser(){
        return jwtParser;
    }

    private Key getSigningKey() {
        return signingKey;
    }

    public Integer getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public Integer getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
}
