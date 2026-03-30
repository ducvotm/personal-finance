package com.example.finance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration}")
    private long jwtAccessExpirationMs;

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        log.info("🔐 JWT PROVIDER: Generating token for user: {} (ID: {})", userPrincipal.getUsername(),
                userPrincipal.getId());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessExpirationMs);

        String token = Jwts.builder().subject(Long.toString(userPrincipal.getId()))
                .claim("username", userPrincipal.getUsername()).claim("email", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole()).issuedAt(now).expiration(expiryDate).signWith(getSigningKey())
                .compact();

        log.debug("📝 JWT Payload - Issued: {}, Expires: {}", now, expiryDate);
        log.info("✅ JWT PROVIDER: Token generated successfully for user: {}", userPrincipal.getUsername());

        return token;
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

        Long userId = Long.parseLong(claims.getSubject());
        log.debug("🔍 JWT PROVIDER: Extracted user ID from token: {}", userId);

        return userId;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            log.debug("✅ JWT PROVIDER: Token validation successful");
            return true;
        } catch (MalformedJwtException ex) {
            log.error("❌ JWT PROVIDER: Invalid JWT token - MalformedJwtException");
        } catch (ExpiredJwtException ex) {
            log.error("❌ JWT PROVIDER: Invalid JWT token - ExpiredJwtException");
        } catch (UnsupportedJwtException ex) {
            log.error("❌ JWT PROVIDER: Invalid JWT token - UnsupportedJwtException");
        } catch (IllegalArgumentException ex) {
            log.error("❌ JWT PROVIDER: Invalid JWT token - IllegalArgumentException");
        }
        return false;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
