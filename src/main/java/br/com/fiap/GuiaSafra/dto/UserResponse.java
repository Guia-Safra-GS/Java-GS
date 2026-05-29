package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.Role;
import br.com.fiap.GuiaSafra.entity.User;

import java.time.LocalDateTime;

// Nunca expor passwordHash (alem do @JsonIgnore na entity, ele simplesmente não existe aqui).
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
