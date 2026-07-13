package com.example.financas.user.controller;

import com.example.financas.user.domain.dto.CreateUserDTO;
import com.example.financas.user.domain.dto.UpdateUserDTO;
import com.example.financas.user.domain.dto.UserResponseDTO;
import com.example.financas.user.service.SavePhotoService;
import com.example.financas.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SavePhotoService savePhotoService;

    public UserController(UserService userService, SavePhotoService savePhotoService) {
        this.userService = userService;
        this.savePhotoService = savePhotoService;
    }

    @PostMapping()
    public ResponseEntity<UserResponseDTO> create(@RequestBody @Valid CreateUserDTO createUserDTO) {
        UserResponseDTO newUser = this.userService.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PatchMapping("/{id}/photo")
    public ResponseEntity<String> uploadPhoto(@PathVariable @P("id") UUID id, @RequestParam("photo") MultipartFile photo) {
        try {
            String url = savePhotoService.updateUserPhoto(id, photo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing photo" + e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        UserResponseDTO user = this.userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> findAllUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponseDTO> users = this.userService.findAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @RequestBody @Valid UpdateUserDTO updateUserDTO,
            @PathVariable UUID id) {
        UserResponseDTO updatedUser = this.userService.updateUser(id, updateUserDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id){
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
