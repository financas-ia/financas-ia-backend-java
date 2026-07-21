package com.example.financas.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO (
        @NotBlank
        String refreshToken
) {
}
