package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.model.auth.gateways.PasswordResetRepository;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@RequiredArgsConstructor
public class RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;

    /**
     * Genera un token de restablecimiento de contraseña.
     * En producción, el token plano debe enviarse por email, no retornarse en la respuesta.
     * Retorna el token plano para facilitar el desarrollo/pruebas.
     */
    public Mono<String> execute(String email) {
        return userRepository.getByEmail(email)
                .switchIfEmpty(Mono.error(new NotFoundException("EMAIL_NOT_FOUND", "No existe una cuenta con ese email")))
                .flatMap(user -> {
                    String plainToken = UUID.randomUUID().toString();
                    String tokenHash = sha256(plainToken);

                    PasswordReset reset = PasswordReset.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(user.getId())
                            .tokenHash(tokenHash)
                            .expiraEn(LocalDateTime.now().plusMinutes(30))
                            .usado(false)
                            .creadoEn(LocalDateTime.now())
                            .build();

                    return passwordResetRepository.save(reset)
                            .thenReturn(plainToken);
                });
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
