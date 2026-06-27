package com.example.financas.user.service;

import com.example.financas.exceptions.ConflictException;
import com.example.financas.user.domain.dto.CreateUserDTO;
import com.example.financas.user.domain.dto.UserResponseDTO;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO createUser(CreateUserDTO createUserDTO) {
        if (this.userRepository.existsBycpf(createUserDTO.cpf())) {
            throw new ConflictException("CPF already used");
        }

        if (this.userRepository.existsByemail(createUserDTO.email())) {
            throw new ConflictException("Email already used");
        }

        User user = new User();
        user.setCpf(createUserDTO.cpf());
        user.setEmail(createUserDTO.email());
        user.setName(createUserDTO.name());
        user.setDateOfBirth(createUserDTO.dateOfBirth());
        user.setPhoneNumber(createUserDTO.phoneNumber());
        String bcryptPassword = this.passwordEncoder.encode(createUserDTO.password());
        user.setPassword(bcryptPassword);

        User newUser = this.userRepository.save(user);

        return new UserResponseDTO(newUser);
    }
}
