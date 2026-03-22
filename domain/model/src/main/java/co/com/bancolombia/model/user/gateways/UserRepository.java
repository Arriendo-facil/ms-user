package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> saveUser(User user);
    Mono<User> findById(String id);
    Mono<User> getByEmail(String email);
    Mono<User> updateUser(User user);
    Mono<Boolean> desactivateUser(String email);
    Mono<Boolean> activateUser(String email);
    Mono<Void> deleteUser(String email);
}
