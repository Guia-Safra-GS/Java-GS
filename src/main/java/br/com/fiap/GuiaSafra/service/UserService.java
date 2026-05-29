package br.com.fiap.GuiaSafra.service;

import br.com.fiap.GuiaSafra.dto.UserRequest;
import br.com.fiap.GuiaSafra.entity.Role;
import br.com.fiap.GuiaSafra.entity.User;
import br.com.fiap.GuiaSafra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Page<User> searchUsers(String name, String email, Pageable pageable) {
        return userRepository.search(name, email, pageable);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with ID %d not found", id))
        );
    }

    public User createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Email %s is already registered", request.email()));
        }
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        return userRepository.save(request.toEntity(hashedPassword));
    }

    public User updateUser(Long id, UserRequest request) {
        User existing = findUserById(id);
        // Se o email mudou, garante que nao colide com outro usuario
        if (!existing.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Email %s is already registered", request.email()));
        }
        existing.setName(request.name());
        existing.setEmail(request.email());
        existing.setRole(request.role() != null ? request.role() : Role.PRODUCER);
        existing.setPasswordHash(BCrypt.hashpw(request.password(), BCrypt.gensalt()));
        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User with ID %d not found", id));
        }
        userRepository.deleteById(id);
    }
}
