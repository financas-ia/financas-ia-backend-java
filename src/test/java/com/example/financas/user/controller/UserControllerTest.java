package com.example.financas.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.financas.exceptions.ConflictException;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.user.domain.dto.CreateUserDTO;
import com.example.financas.user.domain.dto.UpdateUserDTO;
import com.example.financas.user.domain.dto.UserResponseDTO;
import com.example.financas.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new com.example.financas.exceptions.RestExceptionHandle())
                .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    public void createUser_withValidData_shouldReturnCreated() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "test@example.com",
                "147.421.570-01",
                "Test User",
                "password123",
                LocalDate.of(1990, 1, 1),
                "123456789"
        );

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                createUserDTO.email(),
                createUserDTO.name(),
                "USER",
                createUserDTO.cpf(),
                createUserDTO.phoneNumber(),
                createUserDTO.dateOfBirth()
        );

        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(createUserDTO.email()))
                .andExpect(jsonPath("$.CPF").value(createUserDTO.cpf()));
    }

    @Test
    public void createUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        CreateUserDTO invalidDTO = new CreateUserDTO(
                "invalid-email",
                "",
                "Test User",
                "short",
                null,
                ""
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createUser_whenConflictOccurs_shouldReturnConflict() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "test@example.com",
                "147.421.570-01",
                "Test User",
                "password123",
                LocalDate.of(1990, 1, 1),
                "123456789"
        );

        when(userService.createUser(any(CreateUserDTO.class)))
                .thenThrow(new ConflictException("CPF already used"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("CPF already used"));
    }

    @Test
    public void getUserById_withValidId_shouldReturnUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                userId,
                "test@example.com",
                "Test User",
                "USER",
                "147.421.570-01",
                "123456789",
                LocalDate.of(1990, 1, 1)
        );

        when(userService.getUserById(userId)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userResponseDTO.email()))
                .andExpect(jsonPath("$.CPF").value(userResponseDTO.CPF()));
    }

    @Test
    public void getUserById_withInvalidId_shouldReturnNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.getUserById(userId)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    public void findAllUsers_shouldReturnPageOfUsers() throws Exception {
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                UUID.randomUUID(),
                "test@example.com",
                "Test User",
                "USER",
                "147.421.570-01",
                "123456789",
                LocalDate.of(1990, 1, 1)
        );

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<UserResponseDTO> userPage = new PageImpl<>(List.of(userResponseDTO), pageRequest, 1);

        when(userService.findAllUsers(any())).thenReturn(userPage);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value(userResponseDTO.email()));
    }

    @Test
    public void updateUser_withValidData_shouldReturnUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateUserDTO updateUserDTO = new UpdateUserDTO("new@example.com", "New Name", null, null);
        UserResponseDTO updatedUser = new UserResponseDTO(
                userId,
                updateUserDTO.email(),
                updateUserDTO.name(),
                "USER",
                "147.421.570-01",
                "123456789",
                LocalDate.of(1990, 1, 1)
        );

        when(userService.updateUser(any(UUID.class), any(UpdateUserDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(updatedUser.email()))
                .andExpect(jsonPath("$.name").value(updatedUser.name()));
    }

    @Test
    public void deleteUser_withValidId_shouldReturnNoContent() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());
    }
}