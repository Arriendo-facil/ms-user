package co.com.bancolombia.api.user;

import co.com.bancolombia.api.dto.CreateUserDto;
import co.com.bancolombia.api.dto.ErrorResponse;
import co.com.bancolombia.model.user.User;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserRouterRest {

    @RouterOperations({
        @RouterOperation(
            path = "/api/user",
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
                            schema = @Schema(implementation = User.class)
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
        )
    })
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler userHandler) {
        return route(
                POST("api/user"), userHandler::createUser
        );
    }
}
