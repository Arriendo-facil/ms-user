package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.gateways.PasswordResetRepository;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.user.gateways.PasswordEncoder;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<Void> execute(String plainToken, String newPassword) {
        String tokenHash = sha256(plainToken);

        return passwordResetRepository.findActiveByTokenHash(tokenHash)
                .switchIfEmpty(Mono.error(new ValidationException("INVALID_RESET_TOKEN",
                        "Token invalido, expirado o ya utilizado")))
                .flatMap(reset -> userRepository.findById(reset.getUserId())
                        .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "Usuario no encontrado")))
                        .flatMap(user -> {
                            var updatedUser = user.toBuilder()
                                    .passwordHash(passwordEncoder.encode(newPassword))
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            return userRepository.updateUser(updatedUser)
                                    .then(passwordResetRepository.markAsUsed(tokenHash));
                        })
                );
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
