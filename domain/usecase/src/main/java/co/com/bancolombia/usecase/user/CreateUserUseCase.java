package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.PasswordEncoder;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> execute(User user) {
        return checkEmailNotRegistered(user)
                .map(this::prepareForPersistence)
                .flatMap(userRepository::saveUser);
    }

    private Mono<User> checkEmailNotRegistered(User user) {
        return userRepository.getByEmail(user.getEmail())
                .flatMap(existing -> Mono.<User>error(
                        new IllegalStateException("Ya existe un usuario con el email: " + user.getEmail())
                ))
                .switchIfEmpty(Mono.just(user));
    }

    private User prepareForPersistence(User user) {
        return user.toBuilder()
                .id(UUID.randomUUID().toString())
                .passwordHash(passwordEncoder.encode(user.getPasswordHash()))
                .isActive(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
