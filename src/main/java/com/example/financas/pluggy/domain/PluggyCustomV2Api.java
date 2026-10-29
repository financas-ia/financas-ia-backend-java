package com.example.financas.pluggy.domain;
import ai.pluggy.client.response.TransactionsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.UUID;

public interface PluggyCustomV2Api {
    @GET("/v2/transactions")
    Call<TransactionsResponse> getTransactionsV2(
            @Query("accountId") UUID accountId
    );
}

