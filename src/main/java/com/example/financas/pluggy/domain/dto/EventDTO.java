package com.example.financas.pluggy.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record EventDTO (
        @NotBlank
        String event,

        @NotBlank
        String itemId
){
}
