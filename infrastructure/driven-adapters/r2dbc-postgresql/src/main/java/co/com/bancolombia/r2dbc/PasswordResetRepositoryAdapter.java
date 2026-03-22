package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.model.auth.gateways.PasswordResetRepository;
import co.com.bancolombia.r2dbc.mapper.PasswordResetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class PasswordResetRepositoryAdapter implements PasswordResetRepository {

    private final PasswordResetReactiveRepository repository;
    private final PasswordResetMapper mapper;

    @Override
    public Mono<PasswordReset> save(PasswordReset reset) {
        return repository.save(mapper.toPasswordResetEntity(reset)).map(mapper::toPasswordReset);
    }

    @Override
    public Mono<PasswordReset> findActiveByTokenHash(String tokenHash) {
        return repository.findActiveByTokenHash(tokenHash, LocalDateTime.now()).map(mapper::toPasswordReset);
    }

    @Override
    public Mono<Void> markAsUsed(String tokenHash) {
        return repository.markAsUsed(tokenHash).then();
    }
}
