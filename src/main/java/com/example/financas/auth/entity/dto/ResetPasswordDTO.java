package com.example.financas.auth.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordDTO(
        @NotBlank
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
                message = "A senha deve ter no mínimo 8 caracteres, contendo pelo menos uma letra e um número"
        )
        String newPassword
) {
}
