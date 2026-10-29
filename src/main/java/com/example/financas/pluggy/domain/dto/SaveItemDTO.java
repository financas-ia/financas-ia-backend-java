package com.example.financas.pluggy.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveItemDTO (
        @NotBlank
        String itemId
) {
}
