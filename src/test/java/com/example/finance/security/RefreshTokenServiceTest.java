package com.example.finance.security;

import com.example.finance.entity.RefreshToken;
import com.example.finance.entity.User;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.RefreshTokenRepository;
import com.example.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("u").email("u@x.com").password("p").role("USER")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void createRefreshToken_ReplacesExisting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String token = refreshTokenService.createRefreshToken(1L);

        assertNotNull(token);
        verify(refreshTokenRepository).deleteByUser_Id(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> refreshTokenService.createRefreshToken(99L));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void validateRefreshToken_Valid() {
        RefreshToken stored = RefreshToken.builder().user(testUser).token("abc")
                .expiresAt(LocalDateTime.now().plusDays(1)).build();
        when(refreshTokenRepository.findByUser_Id(1L)).thenReturn(Optional.of(stored));

        assertTrue(refreshTokenService.validateRefreshToken(1L, "abc"));
    }

    @Test
    void validateRefreshToken_WrongToken() {
        RefreshToken stored = RefreshToken.builder().user(testUser).token("abc")
                .expiresAt(LocalDateTime.now().plusDays(1)).build();
        when(refreshTokenRepository.findByUser_Id(1L)).thenReturn(Optional.of(stored));

        assertFalse(refreshTokenService.validateRefreshToken(1L, "other"));
    }

    @Test
    void validateRefreshToken_Expired() {
        RefreshToken stored = RefreshToken.builder().user(testUser).token("abc")
                .expiresAt(LocalDateTime.now().minusDays(1)).build();
        when(refreshTokenRepository.findByUser_Id(1L)).thenReturn(Optional.of(stored));

        assertFalse(refreshTokenService.validateRefreshToken(1L, "abc"));
    }

    @Test
    void deleteRefreshToken_DeletesByUser() {
        refreshTokenService.deleteRefreshToken(1L);

        verify(refreshTokenRepository).deleteByUser_Id(1L);
    }
}
