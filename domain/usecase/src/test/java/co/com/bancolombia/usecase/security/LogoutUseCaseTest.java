package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.model.auth.gateways.RefreshTokenRepository;
import co.com.bancolombia.model.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private LogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCase(refreshTokenRepository);
    }

    @Test
    void execute_withValidToken_revokesToken() {
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .expiraEn(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Mono.just(token));
        when(refreshTokenRepository.revoke("valid-token")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("valid-token"))
                .verifyComplete();
    }

    @Test
    void execute_withInvalidToken_throwsUnauthorizedException() {
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("invalid-token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("INVALID_TOKEN");
                })
                .verify();
    }

    @Test
    void logoutAllDevices_revokesAllTokensForUser() {
        when(refreshTokenRepository.revokeAllByUserId("user-1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.logoutAllDevices("user-1"))
                .verifyComplete();
    }
}
