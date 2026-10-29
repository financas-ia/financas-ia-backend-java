package com.example.financas.pluggy.service;

import ai.pluggy.client.response.WebhookEventPayload;
import ai.pluggy.client.response.WebhookEventType;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.pluggy.domain.dto.SaveItemDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.domain.entity.Item;
import com.example.financas.pluggy.repository.AccountRepository;
import com.example.financas.pluggy.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluggyServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private PluggyService pluggyService;

    @Test
    @DisplayName("startAccount - Deve chamar saveItem, saveAccount e saveTransactions em sequencia")
    void startAccount_Success() throws IOException {
        SaveItemDTO dto = new SaveItemDTO("item-123");
        Item mockItem = new Item();
        mockItem.setPluggyItemId("item-123");

        AccountEntity mockAcc = new AccountEntity();
        List<AccountEntity> accounts = List.of(mockAcc);

        when(webhookService.saveItem(dto)).thenReturn(mockItem);
        when(webhookService.saveAccount("item-123", mockItem)).thenReturn(accounts);

        pluggyService.startAccount(dto);

        verify(webhookService, times(1)).saveItem(dto);
        verify(webhookService, times(1)).saveAccount("item-123", mockItem);
        verify(webhookService, times(1)).saveTransactions("item-123", mockItem, accounts);
    }

    @Test
    @DisplayName("update - Deve atualizar contas e transacoes quando o evento for item/updated")
    void update_ItemUpdated_Success() throws IOException {
        WebhookEventPayload payload = new WebhookEventPayload();
        payload.setItemId("item-123");
        payload.setEvent(WebhookEventType.ITEM_UPDATED);

        Item item = new Item();
        item.setPluggyItemId("item-123");

        when(itemRepository.findByPluggyItemId("item-123")).thenReturn(Optional.of(item));

        pluggyService.update(payload);

        assertEquals("UPDATED", item.getStatus());
        verify(webhookService, times(1)).updateAccount("item-123", item);
        verify(webhookService, times(1)).updateTransicional("item-123", item);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    @DisplayName("update - Deve apenas alterar status para ERROR quando o evento for item/error")
    void update_ItemError_Success() throws IOException {
        WebhookEventPayload payload = new WebhookEventPayload();
        payload.setItemId("item-123");
        payload.setEvent(WebhookEventType.ITEM_ERROR);

        Item item = new Item();
        item.setPluggyItemId("item-123");

        when(itemRepository.findByPluggyItemId("item-123")).thenReturn(Optional.of(item));

        pluggyService.update(payload);

        assertEquals("ERROR", item.getStatus());
        verify(webhookService, never()).updateAccount(any(), any());
        verify(webhookService, never()).updateTransicional(any(), any());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    @DisplayName("update - Deve lancar NotFoundException se o Item nao existir no banco local")
    void update_ItemNotFound_ThrowsException() {
        WebhookEventPayload payload = new WebhookEventPayload();
        payload.setItemId("item-inexistente");

        when(itemRepository.findByPluggyItemId("item-inexistente")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pluggyService.update(payload));
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("findAllAccounts - Deve retornar lista de contas do repositorio")
    void findAllAccounts_Success() {
        AccountEntity acc = new AccountEntity();
        when(accountRepository.findAll()).thenReturn(List.of(acc));

        List<AccountEntity> result = pluggyService.findAllAccounts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository, times(1)).findAll();
    }
}