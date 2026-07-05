package com.example.financas.auth.entity.dto;

import jakarta.validation.constraints.NotBlank;

public record RecoveryPasswordDTO(
        @NotBlank
        String email
) {
}
