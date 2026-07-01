package com.example.financas.auth.entity.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginResponseDTO(
        @NotBlank
        String token
) {

    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
