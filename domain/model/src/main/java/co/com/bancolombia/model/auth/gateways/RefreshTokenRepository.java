package co.com.bancolombia.model.auth.gateways;

import co.com.bancolombia.model.auth.RefreshToken;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository {
    Mono<RefreshToken> save(RefreshToken token);
    Mono<RefreshToken> findByToken(String token);
    Mono<Void> revoke(String token);
    Mono<Void> revokeAllByUserId(String userId);
}
