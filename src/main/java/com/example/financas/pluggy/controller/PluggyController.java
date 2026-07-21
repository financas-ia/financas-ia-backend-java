package com.example.financas.pluggy.controller;

import ai.pluggy.client.PluggyClient;
import ai.pluggy.client.request.CreateConnectTokenRequest;
import ai.pluggy.client.response.ConnectTokenResponse;
import ai.pluggy.client.response.WebhookEventPayload;
import ai.pluggy.client.response.WebhookEventType;
import com.example.financas.pluggy.domain.dto.SaveItemDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.service.PluggyService;
import com.example.financas.user.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;
import retrofit2.Call;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pluggy")
public class PluggyController {

    private final PluggyClient pluggyClient;
    private final PluggyService pluggyService;

    public PluggyController(PluggyClient pluggyClient, PluggyService pluggyService) {
        this.pluggyClient = pluggyClient;
        this.pluggyService = pluggyService;
    }

    @GetMapping("/connect-token")
    public ResponseEntity<?> getConnectToken(@AuthenticationPrincipal User user) {
        try {
            CreateConnectTokenRequest request = new CreateConnectTokenRequest("https://redeemably-overgloomy-sherman.ngrok-free.dev/pluggy/webhook", user.getId().toString());
            Call<ConnectTokenResponse> call = pluggyClient.service().createConnectToken(request);
            Response<ConnectTokenResponse> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return ResponseEntity.ok(response.body());
            } else {
                return ResponseEntity.status(response.code())
                        .body("Error generating token in Pluggy: " + response.message());
            }
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/save-item")
    public ResponseEntity<?> saveItem(
            @RequestBody @Valid SaveItemDTO data
            ) {
        try {
            pluggyService.startAccount(data);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> update(@RequestBody @Valid WebhookEventPayload payload) {
        try{
            this.pluggyService.update(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping()
    public ResponseEntity<?> findAllAccounts() {
        List<AccountEntity> accountEntities = this.pluggyService.findAllAccounts();
        return ResponseEntity.ok().body(accountEntities);
    }
}
