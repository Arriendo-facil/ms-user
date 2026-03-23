package co.com.bancolombia.r2dbc.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import co.com.bancolombia.r2dbc.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserReactiveRepository repository;
    private final UserMapper mapper;

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity entity = mapper.toUserEntity(user).toBuilder().newRecord(true).build();
        return repository.save(entity).map(mapper::toUser);
    }

    @Override
    public Mono<User> findById(String id) {
        return repository.findById(id).map(mapper::toUser);
    }

    @Override
    public Mono<User> getByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toUser);
    }

    @Override
    public Mono<User> updateUser(User user) {
        return repository.save(mapper.toUserEntity(user)).map(mapper::toUser);
    }

    @Override
    public Mono<Boolean> desactivateUser(String email) {
        return repository.deactivateByEmail(email).map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> activateUser(String email) {
        return repository.activateByEmail(email).map(count -> count > 0);
    }

    @Override
    public Mono<Void> deleteUser(String email) {
        return repository.deleteByEmail(email);
    }
}
