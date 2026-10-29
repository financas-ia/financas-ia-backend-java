package com.example.financas.pluggy.domain.dto;

import com.example.financas.pluggy.domain.entity.AccountEntity;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDTO (
        UUID id,

        String pluggyAcountId,

        String type,

        String subtype,

        String number,

        BigDecimal balance,

        String currencyCode,

        String name
) {
    public AccountResponseDTO(AccountEntity data) {
        this (data.getId(), data.getPluggyAcountId(), data.getType(), data.getSubtype(), data.getNumber(), data.getBalance(), data.getCurrencyCode(), data.getName());
    }
}
