package com.example.financas.user.service;

import com.example.financas.exceptions.ConflictException;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.user.domain.dto.CreateUserDTO;
import com.example.financas.user.domain.dto.UpdateUserDTO;
import com.example.financas.user.domain.dto.UserResponseDTO;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setCpf("123.456.789-00");
        user.setName("Test User");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setPhoneNumber("123456789");
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

        when(userRepository.existsByCpf(createUserDTO.cpf())).thenReturn(false);
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

        when(userRepository.existsByCpf(createUserDTO.cpf())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> userService.createUser(createUserDTO));
        assertEquals("CPF already used", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponseDTO() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponseDTO response = userService.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findAllUsers_ShouldReturnPageOfUserResponseDTO() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserResponseDTO> response = userService.findAllUsers(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUserResponseDTO() {
        // Arrange
        UpdateUserDTO updateData = new UpdateUserDTO("new@example.com", "New Name", null, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updateData.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserResponseDTO response = userService.updateUser(userId, updateData);

        // Assert
        assertNotNull(response);
        assertEquals(updateData.email(), user.getEmail());
        assertEquals(updateData.name(), user.getName());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        UpdateUserDTO updateData = new UpdateUserDTO("new@example.com", "New Name", null, null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.updateUser(userId, updateData));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUser_WhenEmailAlreadyExists_ShouldThrowConflictException() {
        // Arrange
        UpdateUserDTO updateData = new UpdateUserDTO("existing@example.com", null, null, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updateData.email())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> userService.updateUser(userId, updateData));
        assertEquals("Email already used", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(updateData.email());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}