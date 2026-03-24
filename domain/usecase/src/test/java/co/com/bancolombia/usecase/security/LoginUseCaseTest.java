package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.model.auth.TokenPair;
import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import co.com.bancolombia.model.exception.ForbiddenException;
import co.com.bancolombia.model.exception.UnauthorizedException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.PasswordEncoder;
import co.com.bancolombia.model.user.gateways.TokenProvider;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProvider tokenProvider;

    private LoginUseCase useCase;

    private final User activeUser = User.builder()
            .id("user-1")
            .email("user@test.com")
            .passwordHash("hashed")
            .isActive(true)
            .build();

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(userRepository, refreshTokenRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void execute_withValidCredentials_returnsTokenPair() {
        RefreshToken savedToken = RefreshToken.builder()
                .token("refresh-token")
                .expiraEn(LocalDateTime.now().plusDays(7))
                .build();

        when(userRepository.getByEmail("user@test.com")).thenReturn(Mono.just(activeUser));
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
        when(tokenProvider.generateAccessToken(activeUser)).thenReturn(Mono.just("access-token"));
        when(refreshTokenRepository.save(any())).thenReturn(Mono.just(savedToken));

        StepVerifier.create(useCase.execute("user@test.com", "plain", "Android"))
                .assertNext(pair -> {
                    assertThat(pair.accessToken()).isEqualTo("access-token");
                    assertThat(pair.refreshToken()).isEqualTo("refresh-token");
                })
                .verifyComplete();
    }

    @Test
    void execute_whenUserNotFound_throwsUnauthorizedException() {
        when(userRepository.getByEmail("noexiste@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("noexiste@test.com", "plain", "Android"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
                })
                .verify();
    }

    @Test
    void execute_whenAccountDisabled_throwsForbiddenException() {
        User inactiveUser = activeUser.toBuilder().isActive(false).build();
        when(userRepository.getByEmail("user@test.com")).thenReturn(Mono.just(inactiveUser));

        StepVerifier.create(useCase.execute("user@test.com", "plain", "Android"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ForbiddenException.class);
                    assertThat(((ForbiddenException) error).getErrorCode()).isEqualTo("ACCOUNT_DISABLED");
                })
                .verify();
    }

    @Test
    void execute_whenPasswordDoesNotMatch_throwsUnauthorizedException() {
        when(userRepository.getByEmail("user@test.com")).thenReturn(Mono.just(activeUser));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        StepVerifier.create(useCase.execute("user@test.com", "wrong", "Android"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
                })
                .verify();
    }
}
