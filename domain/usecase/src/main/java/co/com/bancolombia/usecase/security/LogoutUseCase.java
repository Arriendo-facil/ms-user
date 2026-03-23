package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public Mono<Void> execute(String rawToken) {
        return refreshTokenRepository.findByToken(rawToken)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Token invalido")))
                .flatMap(token -> refreshTokenRepository.revoke(token.getToken()));
    }

    public Mono<Void> logoutAllDevices(String userId) {
        return refreshTokenRepository.revokeAllByUserId(userId);
    }
}
