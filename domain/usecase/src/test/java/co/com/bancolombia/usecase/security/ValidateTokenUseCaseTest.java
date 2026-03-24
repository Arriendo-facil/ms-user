package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.TokenClaims;
import co.com.bancolombia.model.exception.UnauthorizedException;
import co.com.bancolombia.model.user.gateways.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateTokenUseCaseTest {

    @Mock private TokenProvider tokenProvider;

    private ValidateTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateTokenUseCase(tokenProvider);
    }

    @Test
    void execute_withValidToken_returnsClaims() {
        TokenClaims claims = TokenClaims.builder().userId("user-1").build();
        when(tokenProvider.validateToken("valid.token")).thenReturn(Mono.just(claims));

        StepVerifier.create(useCase.execute("valid.token"))
                .assertNext(result -> assertThat(result.getUserId()).isEqualTo("user-1"))
                .verifyComplete();
    }

    @Test
    void execute_withInvalidToken_throwsUnauthorizedException() {
        when(tokenProvider.validateToken("bad.token"))
                .thenReturn(Mono.error(new UnauthorizedException("INVALID_TOKEN", "Token inválido")));

        StepVerifier.create(useCase.execute("bad.token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("INVALID_TOKEN");
                })
                .verify();
    }

    @Test
    void execute_withExpiredToken_throwsUnauthorizedException() {
        when(tokenProvider.validateToken("expired.token"))
                .thenReturn(Mono.error(new UnauthorizedException("TOKEN_EXPIRED", "Token expirado")));

        StepVerifier.create(useCase.execute("expired.token"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(UnauthorizedException.class);
                    assertThat(((UnauthorizedException) error).getErrorCode()).isEqualTo("TOKEN_EXPIRED");
                })
                .verify();
    }
}
