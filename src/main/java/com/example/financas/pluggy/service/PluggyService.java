package com.example.financas.pluggy.service;

import ai.pluggy.client.response.WebhookEventPayload;
import ai.pluggy.client.response.WebhookEventType;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.pluggy.domain.dto.SaveItemDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.domain.entity.Item;
import com.example.financas.pluggy.repository.AccountRepository;
import com.example.financas.pluggy.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class PluggyService {

    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;
    private final WebhookService webhookService;

    public PluggyService(ItemRepository itemRepository, AccountRepository accountRepository, WebhookService webhookService) {
        this.itemRepository = itemRepository;
        this.accountRepository = accountRepository;
        this.webhookService = webhookService;
    }

    public void startAccount(SaveItemDTO data) throws IOException {
        Item item = this.webhookService.saveItem(data);

        List<AccountEntity> savedAccountEntities = this.webhookService.saveAccount(data.itemId(), item);

        this.webhookService.saveTransactions(item.getPluggyItemId(), item, savedAccountEntities);
    }

    @Transactional
    public void update(WebhookEventPayload payload) throws IOException {
        Item item = itemRepository.findByPluggyItemId(payload.getItemId()).
                orElseThrow(() -> new NotFoundException("Not Found Item"));

        if (WebhookEventType.ITEM_UPDATED.equals(payload.getEvent())) {
            this.webhookService.updateAccount(payload.getItemId(), item);
            this.webhookService.updateTransicional(payload.getItemId(), item);

            item.setStatus("UPDATED");
            itemRepository.save(item);
        } else if (WebhookEventType.ITEM_ERROR.equals(payload.getEvent())) {
            item.setStatus("ERROR");
            itemRepository.save(item);
        }
    }

    public List<AccountEntity> findAllAccounts() {
        List<AccountEntity> accountEntities = this.accountRepository.findAll();
        return accountEntities;
    }
}
