package com.example.financas.user.controller;

import com.example.financas.user.domain.dto.CreateUserDTO;
import com.example.financas.user.domain.dto.UpdateUserDTO;
import com.example.financas.user.domain.dto.UserResponseDTO;
import com.example.financas.user.domain.entity.User;
import com.example.financas.user.service.SavePhotoService;
import com.example.financas.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "users", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final UserService userService;
    private final SavePhotoService savePhotoService;

    public UserController(UserService userService, SavePhotoService savePhotoService) {
        this.userService = userService;
        this.savePhotoService = savePhotoService;
    }

    @PostMapping()
    @Operation(summary = "Cria os usuarios", description = "Recebe um DTO de criação de usuarios e retorna a entidade criada sem a senha")
    @ApiResponse(responseCode = "201", description = "Usuario criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Email ou CPF ja está em uso")
    public ResponseEntity<UserResponseDTO> create(@RequestBody @Valid CreateUserDTO createUserDTO) {
        UserResponseDTO newUser = this.userService.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PatchMapping("/{id}/photo")
    @Operation(summary = "Adiciona foto de perfil do usuario", description = "Recebe o id do usuario e adiciona a sua foto de perfil." +
            "Somente o dono da conta e um admin podem acessar")
    @ApiResponse(responseCode = "200", description = "Foto adicionada com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuario com o id repassado não foi encontrado")
    @ApiResponse(responseCode = "403", description = "Usuario não possui autorização para acessar a rota")
    @ApiResponse(responseCode = "500", description = "Erro na API ou no serviço de bucket")
    public ResponseEntity<String> uploadPhoto(@PathVariable @P("id") UUID id, @RequestParam("photo") MultipartFile photo) {
        try {
            String url = savePhotoService.updateUserPhoto(id, photo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing photo" + e.getMessage());
        }
    }


    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuario por id",
            description = "Recebe o id do usuario e retorna suas informações," +
                        " somente o dono da conta e um admin podem acessar")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado e retornado")
    @ApiResponse(responseCode = "401", description = "Usuario não encontrado")
    @ApiResponse(responseCode = "403", description = "Usuario não possui autorização para acessar a rota")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        UserResponseDTO userResponse = this.userService.getUserById(id, user);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retorna todos os usuarios",
            description = "Retorna todos os usuarios do Banco de Dados." +
                        " Somente Admins podem acessar." +
                        " Como o retorno é uma lista, se não houver usuarios ele retorna uma lista vazia")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios retornadas com sucesso")
    @ApiResponse(responseCode = "403", description = "Usuario não possui autorização para acessar a rota")
    public ResponseEntity<Page<UserResponseDTO>> findAllUsers(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponseDTO> users = this.userService.findAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza um usuario por id.",
            description = "Recebe o id do usuario e um DTO de update. " +
                        "Somente Admins e o dono da conta podem acessar.")
    @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuario não encontrado")
    @ApiResponse(responseCode = "403", description = "Usuario não possui autorização para acessar a rota")
    public ResponseEntity<UserResponseDTO> updateUser(
            @RequestBody @Valid UpdateUserDTO updateUserDTO,
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        UserResponseDTO updatedUser = this.userService.updateUser(id, updateUserDTO, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um usuario por id.",
            description = "Recebe o id do usuario e faz o delete." +
                    " Somente um admin e um dono da conta podem acessar." +
                    " Não há retorno")
    @ApiResponse(responseCode = "204", description = "Usuario deletado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuario não encontrado")
    @ApiResponse(responseCode = "403", description = "Usuario não possui autorização para acessar a rota")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal User user){
        this.userService.deleteUser(id, user);
        return ResponseEntity.noContent().build();
    }
}
