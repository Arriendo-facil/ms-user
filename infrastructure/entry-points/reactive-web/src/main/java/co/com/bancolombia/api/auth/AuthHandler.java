package co.com.bancolombia.api.auth;

import co.com.bancolombia.api.dto.ErrorResponse;
import co.com.bancolombia.api.dto.LoginRequest;
import co.com.bancolombia.api.dto.MessageResponse;
import co.com.bancolombia.api.dto.PasswordResetRequest;
import co.com.bancolombia.api.dto.RefreshRequest;
import co.com.bancolombia.api.dto.ResetPasswordRequest;
import co.com.bancolombia.api.dto.TokenResponse;
import co.com.bancolombia.usecase.security.LoginUseCase;
import co.com.bancolombia.usecase.security.LogoutUseCase;
import co.com.bancolombia.usecase.security.RefreshTokenUseCase;
import co.com.bancolombia.usecase.security.RequestPasswordResetUseCase;
import co.com.bancolombia.usecase.security.ResetPasswordUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;


@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final Validator validator;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .doOnNext(this::validate)
                .flatMap(req -> loginUseCase.execute(req.email(), req.password(),
                        request.headers().firstHeader("User-Agent")))
                .flatMap(pair -> ServerResponse.ok()
                        .bodyValue(new TokenResponse(pair.accessToken(), pair.refreshToken())))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(new ErrorResponse(e.getMessage())))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.status(HttpStatus.FORBIDDEN).bodyValue(new ErrorResponse(e.getMessage())));
    }

    public Mono<ServerResponse> refresh(ServerRequest request) {
        return request.bodyToMono(RefreshRequest.class)
                .doOnNext(this::validate)
                .flatMap(req -> refreshTokenUseCase.execute(req.refreshToken()))
                .flatMap(pair -> ServerResponse.ok()
                        .bodyValue(new TokenResponse(pair.accessToken(), pair.refreshToken())))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(new ErrorResponse(e.getMessage())));
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        return request.bodyToMono(RefreshRequest.class)
                .doOnNext(this::validate)
                .flatMap(req -> logoutUseCase.execute(req.refreshToken()))
                .then(ServerResponse.noContent().build())
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(new ErrorResponse(e.getMessage())));
    }

    public Mono<ServerResponse> requestPasswordReset(ServerRequest request) {
        return request.bodyToMono(PasswordResetRequest.class)
                .doOnNext(this::validate)
                .flatMap(req -> requestPasswordResetUseCase.execute(req.email()))
                .flatMap(token -> ServerResponse.ok().bodyValue(new MessageResponse(
                        "Si el email existe, recibirás las instrucciones para restablecer tu contraseña")))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.ok().bodyValue(new MessageResponse(
                                "Si el email existe, recibirás las instrucciones para restablecer tu contraseña")));
    }

    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        return request.bodyToMono(ResetPasswordRequest.class)
                .doOnNext(this::validate)
                .flatMap(req -> resetPasswordUseCase.execute(req.token(), req.newPassword()))
                .then(ServerResponse.ok().bodyValue(new MessageResponse("Contraseña actualizada correctamente")))
                .onErrorResume(IllegalArgumentException.class, e ->
                        ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(new ErrorResponse(e.getMessage())));
    }

    private void validate(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
