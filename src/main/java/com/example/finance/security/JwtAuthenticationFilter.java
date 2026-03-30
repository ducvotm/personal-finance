package com.example.finance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("📥 JWT FILTER: Incoming request - {} {}", request.getMethod(), request.getRequestURI());

            String jwt = getJwtFromRequest(request);

            if (jwt != null) {
                log.debug("🔍 JWT token found in Authorization header");
                log.trace("📋 Token (first 50 chars): {}", jwt.substring(0, Math.min(50, jwt.length())));
            } else {
                log.debug("🔓 No JWT token found in request");
            }

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                log.debug("✅ JWT token validation successful");

                Long userId = tokenProvider.getUserIdFromToken(jwt);
                log.info("👤 Token belongs to user ID: {}", userId);

                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                log.info("✅ User loaded from database: {}", userDetails.getUsername());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("🔐 User authenticated successfully: {}", userDetails.getUsername());
            } else {
                if (jwt != null) {
                    log.warn("❌ JWT token validation failed");
                }
            }
        } catch (Exception ex) {
            log.error("❌ Could not set user authentication in security context: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
