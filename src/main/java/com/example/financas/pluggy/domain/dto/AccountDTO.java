package com.example.financas.pluggy.domain.dto;

import com.example.financas.pluggy.domain.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountDTO (
    @NotBlank
    String type,

    @NotBlank
    String subtype,

    @NotBlank
    String number,

    @NotBlank
    Double balance,

    @NotBlank
    String currencyCode,

    @NotBlank
    String name,

    @NotNull
    Item item
){
}
