package com.example.financas.user.service;

import com.example.financas.exceptions.ConflictException;
import com.example.financas.user.domain.dto.CreateUserDTO;
import com.example.financas.user.domain.dto.UserResponseDTO;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_Success() {
        // Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "test@example.com",
                "123.456.789-00",
                "Test User",
                "password123",
                LocalDate.of(1990, 1, 1),
                "123456789"
        );

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCpf(createUserDTO.cpf());
        user.setEmail(createUserDTO.email());
        user.setName(createUserDTO.name());
        user.setDateOfBirth(createUserDTO.dateOfBirth());
        user.setPhoneNumber(createUserDTO.phoneNumber());
        user.setPassword("encodedPassword");

        when(userRepository.existsBycpf(createUserDTO.cpf())).thenReturn(false);
        when(userRepository.existsByEmail(createUserDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(createUserDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponseDTO response = userService.createUser(createUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(createUserDTO.email(), response.email());
        assertEquals(createUserDTO.cpf(), response.CPF());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmailDuplicated() {
        // Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "test@example.com",
                "123.456.789-00",
                "Test User",
                "password123",
                LocalDate.of(1990, 1, 1),
                "123456789"
        );

        when(userRepository.existsByEmail(createUserDTO.email())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> userService.createUser(createUserDTO));
        assertEquals("Email already used", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_CpfDuplicated() {
        // Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "test@example.com",
                "123.456.789-00",
                "Test User",
                "password123",
                LocalDate.of(1990, 1, 1),
                "123456789"
        );

        when(userRepository.existsBycpf(createUserDTO.cpf())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> userService.createUser(createUserDTO));
        assertEquals("CPF already used", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}