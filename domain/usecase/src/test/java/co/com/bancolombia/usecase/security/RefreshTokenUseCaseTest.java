package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import co.com.bancolombia.model.exception.ForbiddenException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.UnauthorizedException;
import co.com.bancolombia.model.user.User;
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
class RefreshTokenUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenProvider tokenProvider;

    private RefreshTokenUseCase useCase;

    private final User activeUser = User.builder()
            .id("user-1")
            .isActive(true)
            .build();

    private final RefreshToken validToken = RefreshToken.builder()
            .token("old-token")
            .userId("user-1")
            .revocado(false)
            .dispositivo("Android")
            .expiraEn(LocalDateTime.now().plusDays(7))
            .build();

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(userRepository, refreshTokenRepository, tokenProvider);
    }

    @Test
    void execute_withValidToken_returnsNewTokenPair() {
        RefreshToken newToken = RefreshToken.builder().token("new-token").build();

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Mono.just(validToken));
        when(refreshTokenRepository.revoke("old-token")).thenReturn(Mono.empty());
        when(userRepository.findById("user-1")).thenReturn(Mono.just(activeUser));
        when(tokenProvider.generateAccessToken(activeUser)).thenReturn(Mono.just("new-access-token"));
        when(refreshTokenRepository.save(any())).thenReturn(Mono.just(newToken));

        StepVerifier.create(useCase.execute("old-token"))
                .assertNext(pair -> {
                    assertThat(pair.accessToken()).isEqualTo("new-access-token");
                    assertThat(pair.refreshToken()).isEqualTo("new-token");
                })
                .verifyComplete();
    }

    @Test
    void execute_withInvalidToken_throwsUnauthorizedException() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("bad-token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("INVALID_TOKEN");
                })
                .verify();
    }

    @Test
    void execute_withRevokedToken_throwsUnauthorizedException() {
        RefreshToken revoked = validToken.toBuilder().revocado(true).build();
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Mono.just(revoked));

        StepVerifier.create(useCase.execute("old-token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("TOKEN_REVOKED");
                })
                .verify();
    }

    @Test
    void execute_withExpiredToken_throwsUnauthorizedException() {
        RefreshToken expired = validToken.toBuilder().expiraEn(LocalDateTime.now().minusDays(1)).build();
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Mono.just(expired));

        StepVerifier.create(useCase.execute("old-token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("TOKEN_EXPIRED");
                })
                .verify();
    }

    @Test
    void execute_whenUserNotFound_throwsNotFoundException() {
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Mono.just(validToken));
        when(refreshTokenRepository.revoke("old-token")).thenReturn(Mono.empty());
        when(userRepository.findById("user-1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("old-token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NotFoundException.class);
                    assertThat(((NotFoundException) error).getErrorCode()).isEqualTo("USER_NOT_FOUND");
                })
                .verify();
    }

    @Test
    void execute_whenUserDisabled_throwsForbiddenException() {
        User inactiveUser = activeUser.toBuilder().isActive(false).build();
        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Mono.just(validToken));
        when(refreshTokenRepository.revoke("old-token")).thenReturn(Mono.empty());
        when(userRepository.findById("user-1")).thenReturn(Mono.just(inactiveUser));

        StepVerifier.create(useCase.execute("old-token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ForbiddenException.class);
                    assertThat(((ForbiddenException) error).getErrorCode()).isEqualTo("ACCOUNT_DISABLED");
                })
                .verify();
    }
}
