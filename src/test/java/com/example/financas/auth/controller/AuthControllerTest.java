package com.example.financas.auth.controller;

import com.example.financas.auth.entity.dto.LoginDTO;
import com.example.financas.auth.entity.dto.LoginResponseDTO;
import com.example.financas.auth.entity.dto.RefreshTokenRequestDTO;
import com.example.financas.auth.entity.dto.TwoFactorDTO;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}