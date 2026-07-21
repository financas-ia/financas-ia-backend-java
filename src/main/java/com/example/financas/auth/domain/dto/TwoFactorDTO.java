package com.example.financas.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorDTO (
        @NotBlank
        String code
) {
    public TwoFactorDTO(String code) {
        this.code = code;
    }
}
