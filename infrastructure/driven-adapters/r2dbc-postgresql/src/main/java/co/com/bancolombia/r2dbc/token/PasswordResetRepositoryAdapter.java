package co.com.bancolombia.r2dbc.token;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.model.auth.gateways.PasswordResetRepository;
import co.com.bancolombia.r2dbc.mapper.PasswordResetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PasswordResetRepositoryAdapter implements PasswordResetRepository {

    private final PasswordResetReactiveRepository repository;
    private final PasswordResetMapper mapper;

    @Override
    public Mono<PasswordReset> save(PasswordReset reset) {
        return Mono.defer(() -> repository.save(mapper.toPasswordResetEntity(reset)))
                .map(mapper::toPasswordReset)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[PasswordResetRepository] Reintento #{} en save() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<PasswordReset> findActiveByTokenHash(String tokenHash) {
        return Mono.defer(() -> repository.findActiveByTokenHash(tokenHash, LocalDateTime.now()))
                .map(mapper::toPasswordReset)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(150))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[PasswordResetRepository] Reintento #{} en findActiveByTokenHash() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<Void> markAsUsed(String tokenHash) {
        return Mono.defer(() -> repository.markAsUsed(tokenHash))
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[PasswordResetRepository] Reintento #{} en markAsUsed() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())))
                .then();
    }
}
