package com.example.financas.pluggy.controller;

import ai.pluggy.client.PluggyApiService;
import ai.pluggy.client.PluggyClient;
import ai.pluggy.client.request.CreateConnectTokenRequest;
import ai.pluggy.client.response.ConnectTokenResponse;
import ai.pluggy.client.response.WebhookEventPayload;
import com.example.financas.pluggy.domain.dto.SaveItemDTO;
import com.example.financas.pluggy.domain.entity.AccountEntity;
import com.example.financas.pluggy.service.PluggyService;
import com.example.financas.user.domain.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PluggyControllerTest {

    @Mock
    private PluggyClient pluggyClient;

    @Mock
    private PluggyService pluggyService;

    @InjectMocks
    private PluggyController pluggyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockUser; // Usuário padrão para injetar nos testes

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 1. Criamos um usuário válido com ID preenchido
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        // 2. Criamos um resolver fake para o Spring injetar este user no controller durante os testes
        HandlerMethodArgumentResolver userArgumentResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                // Se o parâmetro do controller for do tipo User ou anotado com @AuthenticationPrincipal
                return parameter.getParameterType().isAssignableFrom(User.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return mockUser; // Injeta sempre nosso mockUser com ID preenchido
            }
        };

        // 3. Registramos o resolver no MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(pluggyController)
                .setCustomArgumentResolvers(userArgumentResolver)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getConnectToken_Success() throws Exception {
        PluggyApiService apiServiceMock = mock(PluggyApiService.class);
        ConnectTokenResponse tokenResponse = mock(ConnectTokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn("token");

        Call<ConnectTokenResponse> callMock = mock(Call.class);

        when(pluggyClient.service()).thenReturn(apiServiceMock);
        when(apiServiceMock.createConnectToken(any(CreateConnectTokenRequest.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(Response.success(tokenResponse));

        mockMvc.perform(get("/pluggy/connect-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"));
    }

    @Test
    void getConnectToken_Error() throws Exception {
        PluggyApiService apiServiceMock = mock(PluggyApiService.class);
        Call<ConnectTokenResponse> callMock = mock(Call.class);

        when(pluggyClient.service()).thenReturn(apiServiceMock);
        when(apiServiceMock.createConnectToken(any(CreateConnectTokenRequest.class))).thenReturn(callMock);
        when(callMock.execute()).thenThrow(new IOException("Connection error"));

        mockMvc.perform(get("/pluggy/connect-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Connection error"));
    }

    @Test
    void saveItem_Success() throws Exception {
        SaveItemDTO saveItemDTO = new SaveItemDTO("itemId");

        doNothing().when(pluggyService).startAccount(any(SaveItemDTO.class));

        mockMvc.perform(post("/pluggy/save-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveItemDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void saveItem_Error() throws Exception {
        SaveItemDTO saveItemDTO = new SaveItemDTO("itemId");

        doThrow(new RuntimeException("Error saving item")).when(pluggyService).startAccount(any(SaveItemDTO.class));

        mockMvc.perform(post("/pluggy/save-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveItemDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error saving item"));
    }

    @Test
    void update_Success() throws Exception {
        WebhookEventPayload payload = new WebhookEventPayload();

        doNothing().when(pluggyService).update(any(WebhookEventPayload.class));

        mockMvc.perform(post("/pluggy/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    void update_Error() throws Exception {
        WebhookEventPayload payload = new WebhookEventPayload();

        doThrow(new RuntimeException("Error updating")).when(pluggyService).update(any(WebhookEventPayload.class));

        mockMvc.perform(post("/pluggy/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error updating"));
    }

    @Test
    void findAllAccounts_Success() throws Exception {
        AccountEntity account = new AccountEntity();
        account.setId(UUID.randomUUID());
        List<AccountEntity> accounts = Collections.singletonList(account);

        when(pluggyService.findAllAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/pluggy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(account.getId().toString()));
    }

    @Test
    void findAllAccounts_EmptyList() throws Exception {
        when(pluggyService.findAllAccounts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/pluggy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}