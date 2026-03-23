package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Token de recuperación y nueva contraseña")
public record ResetPasswordRequest(
        @NotBlank
        @Schema(description = "Token de restablecimiento recibido por email", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
        String token,

        @NotBlank @Size(min = 8)
        @Schema(description = "Nueva contraseña. Mínimo 8 caracteres.", example = "NuevoPass1!", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
        String newPassword
) {}
