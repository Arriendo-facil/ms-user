package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import co.com.bancolombia.r2dbc.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenReactiveRepository repository;
    private final RefreshTokenMapper mapper;

    @Override
    public Mono<RefreshToken> save(RefreshToken token) {
        return repository.save(mapper.toRefreshTokenEntity(token)).map(mapper::toRefreshToken);
    }

    @Override
    public Mono<RefreshToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toRefreshToken);
    }

    @Override
    public Mono<Void> revoke(String token) {
        return repository.revokeByToken(token).then();
    }

    @Override
    public Mono<Void> revokeAllByUserId(String userId) {
        return repository.revokeAllByUserId(userId).then();
    }
}
