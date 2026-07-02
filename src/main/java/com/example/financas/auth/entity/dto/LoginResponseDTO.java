package com.example.financas.auth.entity.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginResponseDTO(
        @NotBlank
        String acessToken,

        @NotBlank
        String refreshToken
) {

    public LoginResponseDTO(String acessToken, String refreshToken) {
        this.acessToken = acessToken;
        this.refreshToken = refreshToken;
    }
}
