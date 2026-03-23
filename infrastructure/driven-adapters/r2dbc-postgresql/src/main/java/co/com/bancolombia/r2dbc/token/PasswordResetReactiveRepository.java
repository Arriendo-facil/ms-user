package co.com.bancolombia.r2dbc.token;

import co.com.bancolombia.r2dbc.entity.PasswordResetEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PasswordResetReactiveRepository extends ReactiveCrudRepository<PasswordResetEntity, String> {

    @Query("SELECT * FROM password_reset_tokens WHERE token_hash = :tokenHash AND usado = false AND expira_en > :now LIMIT 1")
    Mono<PasswordResetEntity> findActiveByTokenHash(String tokenHash, LocalDateTime now);

    @Modifying
    @Query("UPDATE password_reset_tokens SET usado = true WHERE token_hash = :tokenHash")
    Mono<Integer> markAsUsed(String tokenHash);
}
