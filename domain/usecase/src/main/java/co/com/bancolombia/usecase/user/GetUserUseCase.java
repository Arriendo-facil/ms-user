package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetUserUseCase {
    private final UserRepository userRepository;

    public Mono<User> execute(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "User not found")));
    }
}
