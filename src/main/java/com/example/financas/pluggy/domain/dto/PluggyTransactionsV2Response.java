package com.example.financas.pluggy.domain.dto;

import java.util.List;

public record PluggyTransactionsV2Response(
        List<com.example.financas.pluggy.domain.dto.PluggyTransactionV2Item> results,
        Integer page,
        Integer totalPages,
        Integer total
) {}