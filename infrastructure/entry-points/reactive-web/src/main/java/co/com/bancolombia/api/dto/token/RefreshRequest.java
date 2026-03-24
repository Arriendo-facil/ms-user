package co.com.bancolombia.api.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Token de refresco para renovar o cerrar sesión")
public record RefreshRequest(
        @NotBlank
        @Schema(description = "RefreshToken JWT obtenido en el login o en un refresh anterior", example = "eyJhbGciOiJSUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {}
