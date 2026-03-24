package co.com.bancolombia.api.auth;

import co.com.bancolombia.api.dto.common.ErrorResponse;
import co.com.bancolombia.api.dto.token.TokenValidationResponse;
import co.com.bancolombia.api.dto.user.LoginRequest;
import co.com.bancolombia.api.dto.token.MessageResponse;
import co.com.bancolombia.api.dto.token.PasswordResetRequest;
import co.com.bancolombia.api.dto.token.RefreshRequest;
import co.com.bancolombia.api.dto.token.ResetPasswordRequest;
import co.com.bancolombia.api.dto.token.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthRouterRest {

    @RouterOperations({
        @RouterOperation(
            path = "/api/auth/login",
            method = RequestMethod.POST,
            beanClass = AuthHandler.class,
            beanMethod = "login",
            operation = @Operation(
                operationId = "login",
                summary = "Autenticar usuario",
                description = "Autentica al usuario con email y contraseña. Devuelve un par de tokens JWT: "
                            + "accessToken (vida corta) y refreshToken (vida larga).",
                tags = {"Autenticación"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = LoginRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Autenticación exitosa. Retorna el par de tokens JWT.",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Datos de entrada inválidos",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Credenciales incorrectas (email o contraseña inválidos)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "Cuenta desactivada o sin permisos de acceso",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/api/auth/refresh",
            method = RequestMethod.POST,
            beanClass = AuthHandler.class,
            beanMethod = "refresh",
            operation = @Operation(
                operationId = "refreshToken",
                summary = "Renovar tokens de acceso",
                description = "Genera un nuevo par de tokens JWT usando un refreshToken válido y no expirado. "
                            + "El refreshToken anterior queda invalidado (rotación de tokens).",
                tags = {"Autenticación"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = RefreshRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Tokens renovados exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Cuerpo de la petición inválido",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "RefreshToken inválido, expirado o ya utilizado",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/api/auth/logout",
            method = RequestMethod.POST,
            beanClass = AuthHandler.class,
            beanMethod = "logout",
            operation = @Operation(
                operationId = "logout",
                summary = "Cerrar sesión",
                description = "Invalida el refreshToken proporcionado, impidiendo su uso futuro para renovar tokens. "
                            + "El accessToken seguirá siendo válido hasta su expiración natural.",
                tags = {"Autenticación"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = RefreshRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "204",
                        description = "Sesión cerrada exitosamente. Sin cuerpo de respuesta."
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Cuerpo de la petición inválido",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "RefreshToken inválido o ya expirado",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/api/auth/password-reset/request",
            method = RequestMethod.POST,
            beanClass = AuthHandler.class,
            beanMethod = "requestPasswordReset",
            operation = @Operation(
                operationId = "requestPasswordReset",
                summary = "Solicitar restablecimiento de contraseña",
                description = "Inicia el flujo de recuperación de contraseña enviando un token al email registrado. "
                            + "Por seguridad, la respuesta es siempre exitosa independientemente de si el email existe.",
                tags = {"Autenticación"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = PasswordResetRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Solicitud procesada. Se envían instrucciones si el email está registrado.",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Email con formato inválido",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/api/auth/validate",
            method = RequestMethod.GET,
            beanClass = AuthHandler.class,
            beanMethod = "validateToken",
            operation = @Operation(
                operationId = "validateToken",
                summary = "Validar token JWT",
                description = "Valida un token JWT y retorna los claims del usuario. "
                            + "Usado principalmente por el API Gateway para autorizar requests entrantes. "
                            + "El token debe enviarse en el header Authorization como Bearer token.",
                tags = {"Autenticación"},
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Token válido. Retorna los claims del usuario.",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenValidationResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Token ausente, inválido o expirado",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/api/auth/password-reset/confirm",
            method = RequestMethod.POST,
            beanClass = AuthHandler.class,
            beanMethod = "resetPassword",
            operation = @Operation(
                operationId = "confirmPasswordReset",
                summary = "Confirmar nueva contraseña",
                description = "Establece una nueva contraseña usando el token de recuperación recibido por email. "
                            + "El token es de un solo uso y tiene tiempo de expiración.",
                tags = {"Autenticación"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ResetPasswordRequest.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Contraseña actualizada correctamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Token inválido, expirado, o nueva contraseña no cumple los requisitos mínimos",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        )
    })
    @Bean
    public RouterFunction<ServerResponse> authRouter(AuthHandler authHandler) {
        return route(POST("/api/auth/login"), authHandler::login)
                .andRoute(POST("/api/auth/refresh"), authHandler::refresh)
                .andRoute(POST("/api/auth/logout"), authHandler::logout)
                .andRoute(GET("/api/auth/validate"), authHandler::validateToken)
                .andRoute(POST("/api/auth/password-reset/request"), authHandler::requestPasswordReset)
                .andRoute(POST("/api/auth/password-reset/confirm"), authHandler::resetPassword);
    }
}
