package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.model.auth.TokenPair;
import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.TokenProvider;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public Mono<TokenPair> execute(String rawToken) {
        return refreshTokenRepository.findByToken(rawToken)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Token invalido")))
                .filter(t -> !t.isRevocado())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Token revocado")))
                .filter(t -> t.getExpiraEn().isAfter(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Token expirado")))
                .flatMap(this::revokeAndRenew);
    }

    private Mono<TokenPair> revokeAndRenew(RefreshToken oldToken) {
        String newRawToken = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(oldToken.getUserId())
                .token(newRawToken)
                .expiraEn(LocalDateTime.now().plusDays(7))
                .revocado(false)
                .dispositivo(oldToken.getDispositivo())
                .creadoEn(LocalDateTime.now())
                .build();

        return refreshTokenRepository.revoke(oldToken.getToken())
                .then(userRepository.findById(oldToken.getUserId()))
                .switchIfEmpty(Mono.error(new IllegalStateException("Usuario no encontrado")))
                .filter(User::isActive)
                .switchIfEmpty(Mono.error(new IllegalStateException("Cuenta desactivada")))
                .flatMap(user -> Mono.zip(
                        tokenProvider.generateAccessToken(user),
                        refreshTokenRepository.save(newRefreshToken),
                        (accessToken, saved) -> new TokenPair(accessToken, saved.getToken())
                ));
    }
}
