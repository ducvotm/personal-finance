package com.example.finance.security;

import com.example.finance.entity.RefreshToken;
import com.example.finance.entity.User;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.RefreshTokenRepository;
import com.example.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_TTL_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public String createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("REFRESH TOKEN: Creating token for user ID: {}", userId);

        refreshTokenRepository.deleteByUser_Id(userId);

        String token = UUID.randomUUID().toString();
        RefreshToken entity = RefreshToken.builder().user(user).token(token)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_TTL_DAYS)).build();
        refreshTokenRepository.save(entity);

        log.info("REFRESH TOKEN: Stored for user: {}", userId);

        return token;
    }

    @Transactional(readOnly = true)
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        log.debug("REFRESH TOKEN: Validating for user ID: {}", userId);

        return refreshTokenRepository.findByUser_Id(userId).filter(rt -> rt.getToken().equals(refreshToken))
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now())).isPresent();
    }

    @Transactional
    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
        log.info("REFRESH TOKEN: Deleted for user: {}", userId);
    }
}
