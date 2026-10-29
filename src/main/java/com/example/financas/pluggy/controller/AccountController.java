package com.example.financas.pluggy.controller;

import com.example.financas.pluggy.domain.dto.AccountResponseDTO;
import com.example.financas.pluggy.service.AccountService;
import com.example.financas.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/account")
@Tag(name = "account", description = "Endpoints para gerenciamento de contas em banco do usuario")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Rota para busca de conta por usuario",
            description = "Recebe o id do usuario e retorna todas as contas em banco do usuario. " +
                    "Somente admins e o dono da conta podem acessar a rota")
    @ApiResponse(responseCode = "200", description = "Contas retornadas com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuario não possui autorização para acessar a rota")
    public ResponseEntity<List<AccountResponseDTO>> findByUserId (@PathVariable("id") UUID id, @AuthenticationPrincipal User user) {
        List<AccountResponseDTO> accounts = this.accountService.findById(id, user);
        return ResponseEntity.ok().body(accounts);
    }
}
