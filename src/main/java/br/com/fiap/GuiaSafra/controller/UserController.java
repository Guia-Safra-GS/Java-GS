package br.com.fiap.GuiaSafra.controller;

import br.com.fiap.GuiaSafra.dto.UserRequest;
import br.com.fiap.GuiaSafra.dto.UserResponse;
import br.com.fiap.GuiaSafra.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "CRUD dos produtores/operadores do AgroMonitor, com paginação, busca por filtros, senha hasheada via BCrypt e links HATEOAS")
public class UserController {
    private final UserService userService;

    // Monta a representacao HATEOAS de um usuario: o recurso + links de navegacao.
    private EntityModel<UserResponse> toModel(UserResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(UserController.class).findById(response.id())).withSelfRel(),
                linkTo(methodOn(UserController.class).findAll(null, null, Pageable.unpaged())).withRel("usuarios"));
    }

    @GetMapping
    @Operation(summary = "Lista usuários com paginação e filtros opcionais por nome e email")
    public Page<EntityModel<UserResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            Pageable pageable
    ) {
        return userService.searchUsers(name, email, pageable)
                .map(UserResponse::fromEntity)
                .map(this::toModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public EntityModel<UserResponse> findById(@PathVariable Long id) {
        return toModel(UserResponse.fromEntity(userService.findUserById(id)));
    }

    @PostMapping
    @Operation(summary = "Cria um novo usuário (senha é hasheada com BCrypt antes de salvar)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (email inválido, campos obrigatórios)"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<EntityModel<UserResponse>> create(@RequestBody @Valid UserRequest request) {
        EntityModel<UserResponse> model = toModel(UserResponse.fromEntity(userService.createUser(request)));
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuário existente (senha é re-hasheada)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado por outro usuário")
    })
    public EntityModel<UserResponse> update(@PathVariable Long id, @RequestBody @Valid UserRequest request) {
        return toModel(UserResponse.fromEntity(userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
