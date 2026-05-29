package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.Role;
import br.com.fiap.GuiaSafra.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Name is mandatory")
        @Size(max = 100, message = "Name must have a max of 100 characters")
        String name,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email must be valid")
        @Size(max = 120, message = "Email must have a max of 120 characters")
        String email,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 8, max = 100, message = "Password must have between 8 and 100 characters")
        String password,

        // Quando role não é enviado na requisição o usuario é cadastrado como PRODUCER (default da TB_CAD_USER)
        Role role
) {
    public User toEntity(String hashedPassword) {
        return User.builder()
                .name(name)
                .email(email)
                .passwordHash(hashedPassword)
                .role(role != null ? role : Role.PRODUCER)
                .build();
    }
}
