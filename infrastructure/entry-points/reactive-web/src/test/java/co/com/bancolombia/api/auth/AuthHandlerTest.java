package co.com.bancolombia.api.auth;

import co.com.bancolombia.api.dto.token.PasswordResetRequest;
import co.com.bancolombia.api.dto.token.RefreshRequest;
import co.com.bancolombia.api.dto.token.ResetPasswordRequest;
import co.com.bancolombia.api.dto.token.TokenResponse;
import co.com.bancolombia.api.dto.user.LoginRequest;
import co.com.bancolombia.model.auth.TokenPair;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.usecase.security.LoginUseCase;
import co.com.bancolombia.usecase.security.LogoutUseCase;
import co.com.bancolombia.usecase.security.RefreshTokenUseCase;
import co.com.bancolombia.usecase.security.RequestPasswordResetUseCase;
import co.com.bancolombia.usecase.security.ResetPasswordUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthHandlerTest {

    @Mock private LoginUseCase loginUseCase;
    @Mock private LogoutUseCase logoutUseCase;
    @Mock private RefreshTokenUseCase refreshTokenUseCase;
    @Mock private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @Mock private ResetPasswordUseCase resetPasswordUseCase;
    @Mock private Validator validator;
    @Mock private ServerRequest serverRequest;
    @Mock private ServerRequest.Headers headers;

    private AuthHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuthHandler(loginUseCase, logoutUseCase, refreshTokenUseCase,
                requestPasswordResetUseCase, resetPasswordUseCase, validator);
        when(validator.validate(any())).thenReturn(Set.of());
    }

    @Test
    void login_withValidCredentials_returns200WithTokenPair() {
        LoginRequest req = new LoginRequest("user@test.com", "pass123");
        TokenPair pair = new TokenPair("access-token", "refresh-token");

        when(serverRequest.bodyToMono(LoginRequest.class)).thenReturn(Mono.just(req));
        when(serverRequest.headers()).thenReturn(headers);
        when(headers.firstHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(loginUseCase.execute("user@test.com", "pass123", "Mozilla/5.0")).thenReturn(Mono.just(pair));

        StepVerifier.create(handler.login(serverRequest))
                .assertNext(response -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void login_withInvalidBody_throwsConstraintViolationException() {
        LoginRequest req = new LoginRequest("", "");
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);

        when(serverRequest.bodyToMono(LoginRequest.class)).thenReturn(Mono.just(req));
        when(validator.validate(any())).thenReturn(Set.of(violation));

        StepVerifier.create(handler.login(serverRequest))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void refresh_withValidToken_returns200WithNewTokenPair() {
        RefreshRequest req = new RefreshRequest("old-token");
        TokenPair pair = new TokenPair("new-access", "new-refresh");

        when(serverRequest.bodyToMono(RefreshRequest.class)).thenReturn(Mono.just(req));
        when(refreshTokenUseCase.execute("old-token")).thenReturn(Mono.just(pair));

        StepVerifier.create(handler.refresh(serverRequest))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }

    @Test
    void logout_withValidToken_returns204() {
        RefreshRequest req = new RefreshRequest("valid-token");

        when(serverRequest.bodyToMono(RefreshRequest.class)).thenReturn(Mono.just(req));
        when(logoutUseCase.execute("valid-token")).thenReturn(Mono.empty());

        StepVerifier.create(handler.logout(serverRequest))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();
    }

    @Test
    void requestPasswordReset_whenEmailExists_returns200WithGenericMessage() {
        PasswordResetRequest req = new PasswordResetRequest("user@test.com");

        when(serverRequest.bodyToMono(PasswordResetRequest.class)).thenReturn(Mono.just(req));
        when(requestPasswordResetUseCase.execute("user@test.com")).thenReturn(Mono.just("plain-token"));

        StepVerifier.create(handler.requestPasswordReset(serverRequest))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }

    @Test
    void requestPasswordReset_whenEmailNotFound_returns200WithSameMessage() {
        PasswordResetRequest req = new PasswordResetRequest("noexiste@test.com");

        when(serverRequest.bodyToMono(PasswordResetRequest.class)).thenReturn(Mono.just(req));
        when(requestPasswordResetUseCase.execute("noexiste@test.com"))
                .thenReturn(Mono.error(new NotFoundException("EMAIL_NOT_FOUND", "No existe")));

        StepVerifier.create(handler.requestPasswordReset(serverRequest))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }

    @Test
    void resetPassword_withValidToken_returns200() {
        ResetPasswordRequest req = new ResetPasswordRequest("valid-token", "NewPass123!");

        when(serverRequest.bodyToMono(ResetPasswordRequest.class)).thenReturn(Mono.just(req));
        when(resetPasswordUseCase.execute("valid-token", "NewPass123!")).thenReturn(Mono.empty());

        StepVerifier.create(handler.resetPassword(serverRequest))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }
}
