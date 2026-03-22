package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<UserEntity> findByEmail(String email);

    @Modifying
    @Query("UPDATE users SET is_active = false WHERE email = :email")
    Mono<Integer> deactivateByEmail(String email);

    @Modifying
    @Query("UPDATE users SET is_active = true WHERE email = :email")
    Mono<Integer> activateByEmail(String email);

    Mono<Void> deleteByEmail(String email);
}
