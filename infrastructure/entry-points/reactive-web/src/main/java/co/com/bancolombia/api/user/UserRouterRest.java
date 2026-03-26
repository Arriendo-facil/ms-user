package co.com.bancolombia.api.user;

import co.com.bancolombia.api.dto.common.ErrorResponse;
import co.com.bancolombia.api.dto.user.CreateUserDto;
import co.com.bancolombia.api.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouterRest {

    private static final String PATH_USER = "/api/v1/user";

    @RouterOperations({
        @RouterOperation(
            path = "/api/v1/user",
            method = RequestMethod.POST,
            beanClass = UserHandler.class,
            beanMethod = "createUser",
            operation = @Operation(
                operationId = "createUser",
                summary = "Registrar nuevo usuario",
                description = "Crea una nueva cuenta de usuario en el sistema. El email debe ser único. "
                            + "La contraseña se almacena hasheada con BCrypt.",
                tags = {"Usuarios"},
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CreateUserDto.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "201",
                        description = "Usuario creado exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Datos de entrada inválidos (violaciones de validación)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "409",
                        description = "Ya existe un usuario registrado con ese email",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Error interno del servidor",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        ),
        @RouterOperation(
            path = "/api/v1/user/{id}",
            method = RequestMethod.GET,
            beanClass = UserHandler.class,
            beanMethod = "getUser",
            operation = @Operation(
                operationId = "getUser",
                summary = "Obtener información pública de un usuario",
                description = "Retorna la información no sensible de un usuario dado su ID. Endpoint de acceso público.",
                tags = {"Usuarios"},
                parameters = {
                    @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "Identificador único del usuario",
                        schema = @Schema(type = "string", example = "1234-asda-3433")
                    )
                },
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Información del usuario obtenida exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "Usuario no encontrado",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Error interno del servidor",
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
    public RouterFunction<ServerResponse> routerFunction(UserHandler userHandler) {
        return route(POST(PATH_USER), userHandler::createUser)
                .andRoute(GET(PATH_USER.concat("/{id}")).and(accept(MediaType.APPLICATION_JSON)), userHandler::getUser);
    }
}
