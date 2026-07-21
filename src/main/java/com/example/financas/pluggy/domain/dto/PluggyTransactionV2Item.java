package com.example.financas.pluggy.domain.dto;

import java.math.BigDecimal;

public record PluggyTransactionV2Item(
        String id,
        String description,
        BigDecimal amount,
        String currencyCode,
        String date,
        String status,
        String category,
        String accountId,
        String type
) {}