package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.RefreshTokenEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RefreshTokenReactiveRepository extends ReactiveCrudRepository<RefreshTokenEntity, String> {

    Mono<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Query("UPDATE refresh_tokens SET revocado = true WHERE token = :token")
    Mono<Integer> revokeByToken(String token);

    @Modifying
    @Query("UPDATE refresh_tokens SET revocado = true WHERE user_id = :userId")
    Mono<Integer> revokeAllByUserId(String userId);
}
