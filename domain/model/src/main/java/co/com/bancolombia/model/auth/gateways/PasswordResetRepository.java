package co.com.bancolombia.model.auth.gateways;

import co.com.bancolombia.model.auth.PasswordReset;
import reactor.core.publisher.Mono;

public interface PasswordResetRepository {
    Mono<PasswordReset> save(PasswordReset reset);
    Mono<PasswordReset> findActiveByTokenHash(String tokenHash);
    Mono<Void> markAsUsed(String tokenHash);
}
