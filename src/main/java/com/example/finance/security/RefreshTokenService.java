package com.example.finance.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 7;

    private final RedisTemplate<String, Object> redisTemplate;

    public String createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + userId;

        log.info("🔄 REFRESH TOKEN: Creating token for user ID: {}", userId);

        redisTemplate.opsForValue().set(key, token, Duration.ofDays(REFRESH_TOKEN_TTL_DAYS));

        log.info("✅ REFRESH TOKEN: Token created and stored in Redis for user: {}", userId);

        return token;
    }

    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String storedToken = (String) redisTemplate.opsForValue().get(key);

        log.debug("🔍 REFRESH TOKEN: Validating token for user ID: {}", userId);

        boolean isValid = refreshToken.equals(storedToken);

        if (isValid) {
            log.info("✅ REFRESH TOKEN: Validation successful for user: {}", userId);
        } else {
            log.warn("❌ REFRESH TOKEN: Validation failed for user: {}", userId);
        }

        return isValid;
    }

    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("🗑️ REFRESH TOKEN: Deleted for user: {}", userId);
    }
}
