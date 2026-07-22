package com.example.financas.pluggy.service;

import ai.pluggy.client.PluggyClient;
import ai.pluggy.client.response.ItemResponse;
import ai.pluggy.client.response.TransactionsResponse;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.exceptions.dto.BadRequestException;
import com.example.financas.pluggy.domain.PluggyCustomV2Api;
import com.example.financas.pluggy.domain.dto.SaveItemDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.domain.entity.Item;
import com.example.financas.pluggy.domain.entity.Transactions;
import com.example.financas.pluggy.repository.AccountRepository;
import com.example.financas.pluggy.repository.ItemRepository;
import com.example.financas.pluggy.repository.TransactionsRepository;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class WebhookService {

    private final PluggyClient pluggyClient;
    private final TransactionsRepository transactionsRepository;
    private final AccountRepository accountRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public WebhookService(PluggyClient pluggyClient, TransactionsRepository transactionsRepository,AccountRepository accountRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.pluggyClient = pluggyClient;
        this.transactionsRepository = transactionsRepository;
        this.accountRepository = accountRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateAccount(String itemId, Item item) throws IOException {
        var accountResp = pluggyClient.service().getAccounts(itemId).execute().body();

        if (accountResp.getResults().isEmpty()) {
            return;
        }

        List<String> pluggyId = accountResp.getResults().stream()
                .map(account -> {
                    return account.getId();
                }).toList();

        List<AccountEntity> existsPluggyIds = accountRepository.findByIds(pluggyId);

        List<AccountEntity> newAccountEntity = accountResp.getResults().stream()
                .map(account -> {

                    Optional<AccountEntity> acc = existsPluggyIds.stream()
                            .filter(c -> c.getPluggyAcountId().equals(account.getId()))
                            .findFirst();

                    if (acc.isPresent()) {
                        AccountEntity exists = acc.get();
                        exists.setBalance(BigDecimal.valueOf(account.getBalance()));
                        return exists;
                    }

                    AccountEntity newAcc = new AccountEntity();
                    newAcc.setSubtype(account.getSubtype());
                    newAcc.setType(account.getType());
                    newAcc.setItem(item);
                    newAcc.setName(account.getName());
                    newAcc.setNumber(account.getNumber());
                    newAcc.setBalance(BigDecimal.valueOf(account.getBalance()));
                    newAcc.setPluggyAcountId(account.getId());
                    newAcc.setCurrencyCode(account.getCurrencyCode());
                    return newAcc;
                }).toList();

        this.accountRepository.saveAll(newAccountEntity);

    }

    @Transactional
    public void updateTransicional(String itemId, Item item) throws IOException {
        retrofit2.Retrofit retrofitV2 = new retrofit2.Retrofit.Builder()
                .baseUrl("https://api.pluggy.ai/")
                .client(pluggyClient.getHttpClient())
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();

        PluggyCustomV2Api v2Api = retrofitV2.create(PluggyCustomV2Api.class);
        List<Transactions>allTransactions = new ArrayList<>();
        List<AccountEntity> ExistingAccount = this.accountRepository.findByItem(item);
        Set<String> existingTransactions = this.transactionsRepository.findByAccount(ExistingAccount);

        for (var account : ExistingAccount) {
            var response = v2Api.getTransactionsV2(UUID.fromString(account.getPluggyAcountId())).execute();

            if (!response.isSuccessful() || response.body() == null) {
                throw new BadRequestException("Error to search transactions " + response.code() + " " + response.message());
            }

            var responseBody = response.body();

            List<Transactions> saveTransactions = responseBody.getResults()
                    .stream()
                    .filter(t -> !existingTransactions.contains(String.valueOf(t.getId())))
                    .map(t -> {
                        Transactions transaction = new Transactions();
                        transaction.setType(t.getType().getValue());
                        transaction.setPluggyTransactionsId(t.getId());
                        transaction.setAccount(account);
                        transaction.setCategory(t.getCategory());
                        transaction.setDescription(t.getDescription());
                        transaction.setAmount(BigDecimal.valueOf(t.getAmount()));
                        return transaction;
                    }).toList();
            allTransactions.addAll(saveTransactions);
        }
        if (!allTransactions.isEmpty()) {
            this.transactionsRepository.saveAll(allTransactions);
        }
    }

    public Item saveItem(SaveItemDTO data) throws IOException {
        Response<ItemResponse> response = pluggyClient.service().getItem(data.itemId()).execute();

        if (!response.isSuccessful() || response.body() == null) {
            String errorDetail = response.errorBody() != null ? response.errorBody().string() : "";
            throw new BadRequestException(response.code() + " " + response.message() + " " + errorDetail);
        }

        ItemResponse item = response.body();
        String userId = item.getClientUserId();
        if (userId == null) {
            throw new BadRequestException("UserId is Null");
        }
        User user = this.userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("Not found User"));

        Item saveItem = new Item();
        saveItem.setPluggyItemId(data.itemId());
        saveItem.setStatus(item.getStatus().getValue());
        saveItem.setInstitutionName(item.getConnector().getName());
        saveItem.setInstitutionImageUrl(item.getConnector().getImageUrl());
        saveItem.setUser(user);
        return this.itemRepository.save(saveItem);
    }

    @Transactional
    public List<AccountEntity> saveAccount(String itemId, Item item) throws IOException {
        var accounts = pluggyClient.service().getAccounts(itemId).execute();

        if (!accounts.isSuccessful() || accounts.body() == null) {
            String errorDetail = accounts.errorBody() != null ? accounts.errorBody().string() : "";
            throw new BadRequestException(accounts.code() + " " + accounts.message() + " " + errorDetail);
        }

        var accountsResp = accounts.body();

        List<AccountEntity> accountEntityToSave = accountsResp.getResults().stream()
                .map(acc -> {
                    AccountEntity cont = new AccountEntity();
                    cont.setType(acc.getType());
                    cont.setSubtype(acc.getSubtype());
                    cont.setBalance(BigDecimal.valueOf(acc.getBalance()));
                    cont.setNumber(acc.getNumber());
                    cont.setItem(item);
                    cont.setCurrencyCode(acc.getCurrencyCode());
                    cont.setName(acc.getName());
                    cont.setPluggyAcountId(acc.getId());
                    return cont;
                }).toList();

        return this.accountRepository.saveAll(accountEntityToSave);
    }

    public void saveTransactions(String itemId, Item item, List<AccountEntity> accountEntityToFind) throws IOException {
        retrofit2.Retrofit retrofitV2 = new retrofit2.Retrofit.Builder()
                .baseUrl("https://api.pluggy.ai/")
                .client(pluggyClient.getHttpClient())
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();

        PluggyCustomV2Api v2Api = retrofitV2.create(PluggyCustomV2Api.class);
        for (var pluggyAccount : accountEntityToFind) {
            var response = v2Api.getTransactionsV2(UUID.fromString(pluggyAccount.getPluggyAcountId())).execute();

            if (!response.isSuccessful() || response.body() == null) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No details";
                throw new BadRequestException("Fail to search transactions " + errorBody);
            }

            TransactionsResponse transactionsResp = response.body();

            List<Transactions> novasTransacoes = transactionsResp.getResults().stream()
                    .map(dto -> {
                        Transactions entidade = new Transactions();
                        entidade.setPluggyTransactionsId(dto.getId());
                        entidade.setDescription(dto.getDescription());
                        entidade.setAmount(BigDecimal.valueOf(dto.getAmount()));
                        entidade.setCategory(dto.getCategory() != null ? dto.getCategory() : "Others");
                        entidade.setType(String.valueOf(dto.getType()));
                        entidade.setDate(OffsetDateTime.parse(dto.getDate()));
                        entidade.setAccount(pluggyAccount);

                        return entidade;
                    })
                    .toList();

            this.transactionsRepository.saveAll(novasTransacoes);
        };
    }
}
