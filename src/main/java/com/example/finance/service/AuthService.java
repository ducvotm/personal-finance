package com.example.finance.service;

import com.example.finance.dto.request.LoginRequest;
import com.example.finance.dto.request.RegisterRequest;
import com.example.finance.dto.response.AuthResponse;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.repository.UserRepository;
import com.example.finance.security.JwtTokenProvider;
import com.example.finance.security.RefreshTokenService;
import com.example.finance.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        log.info("✅ AUTH SERVICE: User {} registered successfully", savedUser.getUsername());

        return AuthResponse.of(accessToken, refreshToken, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String accessToken = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        log.info("✅ AUTH SERVICE: User {} logged in successfully", userPrincipal.getUsername());

        return AuthResponse.of(accessToken, refreshToken, userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail());
    }

    public AuthResponse refreshToken(String refreshToken, Long userId) {
        log.info("🔄 AUTH SERVICE: Refreshing token for user ID: {}", userId);
        
        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            log.warn("❌ AUTH SERVICE: Invalid refresh token for user ID: {}", userId);
            throw new BadRequestException("Invalid refresh token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("❌ AUTH SERVICE: User not found with ID: {}", userId);
                    return new BadRequestException("User not found");
                });

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String newAccessToken = tokenProvider.generateToken(authentication);

        log.info("✅ AUTH SERVICE: Token refreshed successfully for user: {}", user.getUsername());

        return AuthResponse.of(newAccessToken, refreshToken, user.getId(), user.getUsername(), user.getEmail());
    }

    public void logout(Long userId) {
        log.info("🚪 AUTH SERVICE: User {} logging out", userId);
        refreshTokenService.deleteRefreshToken(userId);
        log.info("✅ AUTH SERVICE: User {} logged out successfully", userId);
    }
}
