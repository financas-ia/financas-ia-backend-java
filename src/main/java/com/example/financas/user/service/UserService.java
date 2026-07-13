package com.example.financas.user.service;

import com.example.financas.auth.service.AuthService;
import com.example.financas.exceptions.ConflictException;
import com.example.financas.exceptions.NotFoundException;
import com.example.financas.user.domain.dto.CreateUserDTO;
import com.example.financas.user.domain.dto.UpdateUserDTO;
import com.example.financas.user.domain.dto.UserResponseDTO;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public UserResponseDTO createUser(CreateUserDTO createUserDTO) {
        if (this.userRepository.existsByCpf(createUserDTO.cpf())) {
            throw new ConflictException("CPF already used");
        }

        if (this.userRepository.existsByEmail(createUserDTO.email())) {
            throw new ConflictException("Email already used");
        }

        User user = new User();
        user.setCpf(createUserDTO.cpf());
        user.setEmail(createUserDTO.email());
        user.setName(createUserDTO.name());
        user.setDateOfBirth(createUserDTO.dateOfBirth());
        user.setPhoneNumber(createUserDTO.phoneNumber());
        user.setTwoFactorEnabled(false);
        String bcryptPassword = this.passwordEncoder.encode(createUserDTO.password());
        user.setPassword(bcryptPassword);
        user.setPhotoUrl(null);

        User newUser = this.userRepository.save(user);

        return new UserResponseDTO(newUser);
    }

    public UserResponseDTO getUserById(UUID id) {
        this.authService.validateUser(id);
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserResponseDTO(user);
    }

    public Page<UserResponseDTO> findAllUsers(Pageable pageable) {
        Page<User> users = this.userRepository.findAll(pageable);
        return users.map(UserResponseDTO::new);
    }

    @Transactional
    public UserResponseDTO updateUser(UUID id, UpdateUserDTO data) {
        this.authService.validateUser(id);
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (data.email() != null && !data.email().equals(user.getEmail())) {
            if (this.userRepository.existsByEmail(data.email())) {
                throw new ConflictException("Email already used");
            }
            user.setEmail(data.email());
        }

        if (data.name() != null) {
            user.setName(data.name());
        }

        if (data.dateOfBirth() != null) {
            user.setDateOfBirth(data.dateOfBirth());
        }

        if (data.phoneNumber() != null) {
            user.setPhoneNumber(data.phoneNumber());
        }

        User updatedUser = this.userRepository.save(user);
        return new UserResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        this.authService.validateUser(id);
        if (!this.userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }

        this.userRepository.deleteById(id);
    }


}
