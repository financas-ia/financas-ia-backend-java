package com.example.financas.pluggy.service;

import com.example.financas.auth.service.AuthService;
import com.example.financas.pluggy.domain.dto.AccountResponseDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.repository.AccountRepository;
import com.example.financas.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_withValidUser_shouldReturnAccounts() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(UUID.randomUUID());
        accountEntity.setPluggyAcountId("pluggy123");
        accountEntity.setType("Corrente");
        accountEntity.setSubtype("Pessoal");
        accountEntity.setNumber("12345-6");
        accountEntity.setBalance(new BigDecimal("1000.00"));
        accountEntity.setCurrencyCode("BRL");
        accountEntity.setName("Conta Principal");

        when(accountRepository.findByUser(userId)).thenReturn(List.of(accountEntity));

        List<AccountResponseDTO> result = accountService.findById(userId, user);

        verify(authService, times(1)).validateUser(userId, user);
        verify(accountRepository, times(1)).findByUser(userId);
        assertEquals(1, result.size());
        assertEquals("12345-6", result.get(0).number());
    }

    @Test
    void findById_withInvalidUser_shouldThrowException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        doThrow(new SecurityException("Unauthorized")).when(authService).validateUser(userId, user);

        try {
            accountService.findById(userId, user);
        } catch (SecurityException e) {
            assertEquals("Unauthorized", e.getMessage());
        }

        verify(authService, times(1)).validateUser(userId, user);
        verify(accountRepository, never()).findByUser(any());
    }
}