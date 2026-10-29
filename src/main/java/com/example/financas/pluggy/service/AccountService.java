package com.example.financas.pluggy.service;

import com.example.financas.auth.service.AuthService;
import com.example.financas.pluggy.domain.dto.AccountResponseDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.repository.AccountRepository;
import com.example.financas.user.domain.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthService authService;

    public AccountService(AccountRepository accountRepository, AuthService authService) {
        this.accountRepository = accountRepository;
        this.authService = authService;
    }

    public List<AccountResponseDTO> findById (UUID id, User user) {
        this.authService.validateUser(id, user);
        List<AccountEntity> accounts = this.accountRepository.findByUser(id);
        return accounts
                .stream()
                .map(AccountResponseDTO::new)
                .toList();
    }
}
