package com.example.financas.auth.service;

import com.example.financas.auth.entity.dto.LoginDTO;
import com.example.financas.auth.entity.dto.LoginResponseDTO;
import com.example.financas.auth.entity.dto.TwoFactorDTO;
import com.example.financas.auth.entity.entity.PasswordRecovery;
import com.example.financas.config.jwt.AcessTokenJwt;
import com.example.financas.config.jwt.PreAuthTokenJwt;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AcessTokenJwt acessTokenJwt;

    @Mock
    private PreAuthTokenJwt preAuthTokenJwt;

    @Mock
    private TwoFactorCodeService twoFactorCodeService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordRecoveryService passwordRecoveryService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_TwoFactorDisabled_ShouldReturnLoginResponseDTO() {
        // Given
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password");
        User user = mock(User.class);
        when(user.isTwoFactorEnabled()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        when(refreshTokenService.createRefreshToken(user.getEmail())).thenReturn("refreshToken");
        when(acessTokenJwt.generateToken(user.getEmail())).thenReturn("accessToken");

        // When
        LoginResponseDTO response = authService.login(loginDTO);

        // Then
        verify(refreshTokenService).invalidRefreshToken(user);
        verify(refreshTokenService).saveRefreshToken("refreshToken", user);
        assertEquals("accessToken", response.acessToken());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    void login_TwoFactorEnabled_ShouldReturnTwoFactorDTO() {
        // Given
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.isTwoFactorEnabled()).thenReturn(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null));

        when(twoFactorCodeService.genareteTwoFactorCode()).thenReturn("123456");

        when(preAuthTokenJwt.generatePreAuthToken(user.getEmail())).thenReturn("preAuthToken");

        // When
        TwoFactorDTO response = authService.login(loginDTO);

        // Then
        verify(twoFactorCodeService).genareteTwoFactorCode();
        verify(twoFactorCodeService).SaveTwoFactorCode(eq(user), anyString());
        verify(twoFactorCodeService).sendTwoFactorCode(eq(user.getEmail()), anyString());

        assertEquals("preAuthToken", response.code());
    }

    @Test
    void verifyTwoFactorCode_ValidCode_ShouldReturnLoginResponseDTO() {
        // Given
        User user = mock(User.class);
        when(twoFactorCodeService.validTwoFactorCode(user, "validCode")).thenReturn(true);
        when(refreshTokenService.createRefreshToken(user.getEmail())).thenReturn("refreshToken");
        when(acessTokenJwt.generateToken(user.getEmail())).thenReturn("accessToken");

        // When
        LoginResponseDTO response = authService.verifyTwoFactorCode("validCode", user);

        // Then
        verify(refreshTokenService).invalidRefreshToken(user);
        verify(refreshTokenService).saveRefreshToken("refreshToken", user);
        assertEquals("accessToken", response.acessToken());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    void verifyTwoFactorCode_InvalidCode_ShouldThrowRuntimeException() {
        // Given
        User user = mock(User.class);
        when(twoFactorCodeService.validTwoFactorCode(user, "invalidCode")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.verifyTwoFactorCode("invalidCode", user));
    }

    @Test
    void refreshToken_ValidToken_ShouldReturnLoginResponseDTO() {
        // Given
        User user = mock(User.class);
        when(refreshTokenService.validateRefreshToken("validToken")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(refreshTokenService.createRefreshToken("test@example.com")).thenReturn("newRefreshToken");
        when(acessTokenJwt.generateToken("test@example.com")).thenReturn("newAccessToken");

        // When
        LoginResponseDTO response = authService.refreshToken("validToken");

        // Then
        verify(refreshTokenService).invalidRefreshToken(user);
        verify(refreshTokenService).saveRefreshToken("newRefreshToken", user);
        assertEquals("newAccessToken", response.acessToken());
        assertEquals("newRefreshToken", response.refreshToken());
    }

    @Test
    void refreshToken_UserNotFound_ShouldThrowNotFoundException() {
        // Given
        when(refreshTokenService.validateRefreshToken("validToken")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> authService.refreshToken("validToken"));
    }

    @Test
    void forgorPassword_UserExists_ShouldSendRecoveryEmail() {
        // Given
        String email = "test@example.com";
        User user = mock(User.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordRecoveryService.generateRecoveryToken(user)).thenReturn("recoveryToken");

        // When
        authService.forgorPassword(email);

        // Then
        verify(passwordRecoveryService).invalidAllTokens(user);
        verify(passwordRecoveryService).generateRecoveryToken(user);
        verify(passwordRecoveryService).sendRecoveryEmail(user, "recoveryToken");
    }

    @Test
    void forgorPassword_UserDoesNotExist_ShouldDoNothing() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        authService.forgorPassword(email);

        // Then
        verify(passwordRecoveryService, never()).invalidAllTokens(any());
        verify(passwordRecoveryService, never()).generateRecoveryToken(any());
        verify(passwordRecoveryService, never()).sendRecoveryEmail(any(), any());
    }

    @Test
    void resetPassword_ValidToken_ShouldResetPassword() {
        // Given
        String token = "validToken";
        String newPassword = "newPassword123";
        PasswordRecovery passwordRecovery = mock(PasswordRecovery.class);
        User user = mock(User.class);

        when(passwordRecoveryService.validateAndGetToken(token)).thenReturn(passwordRecovery);
        when(passwordRecovery.getUser()).thenReturn(user);
        when(bCryptPasswordEncoder.encode(newPassword)).thenReturn("hashedPassword");

        // When
        authService.resetPassword(token, newPassword);

        // Then
        verify(user).setPassword("hashedPassword");
        verify(userRepository).save(user);
        verify(passwordRecoveryService).completeRecovery(passwordRecovery);
    }

    @Test
    void resetPassword_InvalidToken_ShouldThrowException() {
        // Given
        String token = "invalidToken";
        String newPassword = "newPassword123";
        when(passwordRecoveryService.validateAndGetToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.resetPassword(token, newPassword));
        assertEquals("Invalid token", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordRecoveryService, never()).completeRecovery(any());
    }


}