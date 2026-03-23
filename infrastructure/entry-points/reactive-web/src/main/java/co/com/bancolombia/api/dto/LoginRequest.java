package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(
        @NotBlank @Email
        @Schema(description = "Email registrado del usuario", example = "maria.garcia@ejemplo.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank
        @Schema(description = "Contraseña del usuario", example = "MiPassw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {}
