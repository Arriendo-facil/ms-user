package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.TokenClaims;
import co.com.bancolombia.model.user.gateways.TokenProvider;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ValidateTokenUseCase {

    private final TokenProvider tokenProvider;

    public Mono<TokenClaims> execute(String token) {
        return tokenProvider.validateToken(token);
    }
}
