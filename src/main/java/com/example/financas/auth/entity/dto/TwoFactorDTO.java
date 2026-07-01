package com.example.financas.auth.entity.dto;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorDTO (
        @NotBlank
        String code
) {
}
