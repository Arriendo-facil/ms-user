package co.com.bancolombia.r2dbc.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import co.com.bancolombia.r2dbc.mapper.UserMapper;
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
public class UserRepositoryAdapter implements UserRepository {

    private final UserReactiveRepository repository;
    private final UserMapper mapper;

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity entity = mapper.toUserEntity(user).toBuilder().newRecord(true).build();
        return Mono.defer(() -> repository.save(entity))
                .map(mapper::toUser)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[UserRepository] Reintento #{} en saveUser() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<User> findById(String id) {
        return Mono.defer(() -> repository.findById(id))
                .map(mapper::toUser)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(150))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[UserRepository] Reintento #{} en findById() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<User> getByEmail(String email) {
        return Mono.defer(() -> repository.findByEmail(email))
                .map(mapper::toUser)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(150))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[UserRepository] Reintento #{} en getByEmail() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<User> updateUser(User user) {
        return Mono.defer(() -> repository.save(mapper.toUserEntity(user)))
                .map(mapper::toUser)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[UserRepository] Reintento #{} en updateUser() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<Boolean> desactivateUser(String email) {
        return Mono.defer(() -> repository.deactivateByEmail(email))
                .map(count -> count > 0)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[UserRepository] Reintento #{} en desactivateUser() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<Boolean> activateUser(String email) {
        return Mono.defer(() -> repository.activateByEmail(email))
                .map(count -> count > 0)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[UserRepository] Reintento #{} en activateUser() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<Void> deleteUser(String email) {
        return Mono.defer(() -> repository.deleteByEmail(email))
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[UserRepository] Reintento #{} en deleteUser() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }
}
