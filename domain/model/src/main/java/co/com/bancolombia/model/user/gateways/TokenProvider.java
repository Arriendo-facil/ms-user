package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> generateAccessToken(User user);
}
