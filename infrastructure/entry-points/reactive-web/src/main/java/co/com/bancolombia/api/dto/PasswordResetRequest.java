package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Email para solicitar el restablecimiento de contraseña")
public record PasswordResetRequest(
        @NotBlank @Email
        @Schema(description = "Email registrado al que se enviará el enlace de recuperación", example = "maria.garcia@ejemplo.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {}
