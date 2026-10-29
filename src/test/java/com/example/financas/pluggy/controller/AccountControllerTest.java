package com.example.financas.pluggy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.financas.exceptions.ForbiddenException;
import com.example.financas.pluggy.domain.dto.AccountResponseDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.service.AccountService;
import com.example.financas.user.domain.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new com.example.financas.exceptions.RestExceptionHandle())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    public void findByUserId_withValidId_shouldReturnAccounts() throws Exception {
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

        AccountResponseDTO accountResponseDTO = new AccountResponseDTO(accountEntity);
        List<AccountResponseDTO> accounts = Collections.singletonList(accountResponseDTO);

        when(accountService.findById(eq(userId), any(User.class))).thenReturn(accounts);

        mockMvc.perform(get("/account/{id}", userId)
                        .principal(() -> userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value("12345-6"))
                .andExpect(jsonPath("$[0].type").value("Corrente"))
                .andExpect(jsonPath("$[0].balance").value(1000.0));
    }

    @Test
    public void findByUserId_withInvalidId_shouldReturnUnauthorized() throws Exception {
        UUID userId = UUID.randomUUID();

        when(accountService.findById(eq(userId), any(User.class)))
                .thenThrow(new ForbiddenException("Acess denied"));

        mockMvc.perform(get("/account/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acess denied"));
    }
}