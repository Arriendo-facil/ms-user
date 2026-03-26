package co.com.bancolombia.r2dbc.token;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import co.com.bancolombia.r2dbc.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenReactiveRepository repository;
    private final RefreshTokenMapper mapper;

    @Override
    public Mono<RefreshToken> save(RefreshToken token) {
        return Mono.defer(() -> repository.save(mapper.toRefreshTokenEntity(token)))
                .map(mapper::toRefreshToken)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[RefreshTokenRepository] Reintento #{} en save() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<RefreshToken> findByToken(String token) {
        return Mono.defer(() -> repository.findByToken(token))
                .map(mapper::toRefreshToken)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(150))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[RefreshTokenRepository] Reintento #{} en findByToken() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<Void> revoke(String token) {
        return Mono.defer(() -> repository.revokeByToken(token))
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[RefreshTokenRepository] Reintento #{} en revoke() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())))
                .then();
    }

    @Override
    public Mono<Void> revokeAllByUserId(String userId) {
        return Mono.defer(() -> repository.revokeAllByUserId(userId))
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[RefreshTokenRepository] Reintento #{} en revokeAllByUserId() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())))
                .then();
    }
}
