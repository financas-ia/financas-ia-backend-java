package com.example.financas.auth.controller;

import com.example.financas.auth.domain.dto.*;
import com.example.financas.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.example.financas.exceptions.RestExceptionHandle())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void login_ShouldReturnLoginResponseDTO_WhenTwoFactorDisabled() throws Exception {
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password");
        LoginResponseDTO loginResponse = new LoginResponseDTO("accessToken", "refreshToken");
        Mockito.when(authService.login(any(LoginDTO.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(loginResponse)));
    }

    @Test
    void login_ShouldReturnTwoFactorDTO_WhenTwoFactorEnabled() throws Exception {
        LoginDTO loginDTO = new LoginDTO("test@example.com", "password");
        TwoFactorDTO twoFactorResponse = new TwoFactorDTO("preAuthToken");
        Mockito.when(authService.login(any(LoginDTO.class))).thenReturn(twoFactorResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(twoFactorResponse)));
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnLoginResponseDTO() throws Exception {
        TwoFactorDTO twoFactorDTO = new TwoFactorDTO("validCode");
        LoginResponseDTO loginResponse = new LoginResponseDTO("accessToken", "refreshToken");
        Mockito.when(authService.verifyTwoFactorCode(eq("validCode"), any())).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(twoFactorDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(loginResponse)));
    }

    @Test
    void refreshToken_ShouldReturnLoginResponseDTO() throws Exception {
        RefreshTokenRequestDTO refreshTokenRequest = new RefreshTokenRequestDTO("validRefreshToken");
        LoginResponseDTO loginResponse = new LoginResponseDTO("newAccessToken", "newRefreshToken");
        Mockito.when(authService.refreshToken(eq("validRefreshToken"))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(loginResponse)));
    }

    @Test
    void requestPasswordRecovery_ShouldReturnOk_WhenEmailExists() throws Exception {
        RecoveryPasswordDTO recoveryPasswordDTO = new RecoveryPasswordDTO("test@example.com");

        mockMvc.perform(post("/auth/request-password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoveryPasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password recovery email sent"));

        Mockito.verify(authService, Mockito.times(1)).forgorPassword(recoveryPasswordDTO.email());
    }

    @Test
    void requestPasswordRecovery_ShouldReturnOk_WhenEmailDoesNotExist() throws Exception {
        RecoveryPasswordDTO recoveryPasswordDTO = new RecoveryPasswordDTO("nonexistent@example.com");

        mockMvc.perform(post("/auth/request-password-recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoveryPasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password recovery email sent"));

        Mockito.verify(authService, Mockito.times(1)).forgorPassword(recoveryPasswordDTO.email());
    }

    @Test
    void resetPassword_ShouldReturnOk_WhenTokenIsValid() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("newPassword123");
        String token = "validToken";

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDTO)))
                .andExpect(status().isOk());

        Mockito.verify(authService, Mockito.times(1)).resetPassword(token, resetPasswordDTO.newPassword());
    }

    @Test
    void resetPassword_ShouldReturnBadRequest_WhenTokenIsInvalid() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO("newPassword123");
        String token = "invalidToken";

        Mockito.doThrow(new com.example.financas.exceptions.NotFoundException("Invalid or expired password recovery token"))
                .when(authService).resetPassword(token, resetPasswordDTO.newPassword());

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invalid or expired password recovery token")); // 🎯 Ajuste a chave do JSON ("detail" ou "message")

        Mockito.verify(authService, Mockito.times(1)).resetPassword(token, resetPasswordDTO.newPassword());
    }
}