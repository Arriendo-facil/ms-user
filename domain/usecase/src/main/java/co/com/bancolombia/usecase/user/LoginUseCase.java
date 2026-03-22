package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.model.auth.TokenPair;
import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.PasswordEncoder;
import co.com.bancolombia.model.user.gateways.TokenProvider;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public Mono<TokenPair> execute(String email, String rawPassword, String dispositivo) {
        return userRepository.getByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Credenciales invalidas")))
                .filter(User::isActive)
                .switchIfEmpty(Mono.error(new IllegalStateException("Cuenta desactivada")))
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Credenciales invalidas")))
                .flatMap(user -> generateTokenPair(user, dispositivo));
    }

    private Mono<TokenPair> generateTokenPair(User user, String dispositivo) {
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .token(rawRefreshToken)
                .expiraEn(LocalDateTime.now().plusDays(7))
                .revocado(false)
                .dispositivo(dispositivo)
                .creadoEn(LocalDateTime.now())
                .build();

        return Mono.zip(
                tokenProvider.generateAccessToken(user),
                refreshTokenRepository.save(refreshToken),
                (accessToken, saved) -> new TokenPair(accessToken, saved.getToken())
        );
    }
}
