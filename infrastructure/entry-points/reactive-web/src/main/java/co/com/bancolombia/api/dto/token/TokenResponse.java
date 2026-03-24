package co.com.bancolombia.api.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Par de tokens JWT devuelto tras autenticación o renovación exitosa")
public record TokenResponse(
        @Schema(description = "Token de acceso JWT de vida corta.", example = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyLWlkIn0...")
        String accessToken,

        @Schema(description = "Token de refresco de vida larga. Usar en /api/auth/refresh o /api/auth/logout.", example = "eyJhbGciOiJSUzI1NiJ9.eyJqdGkiOiJyZWZyZXNoLWlkIn0...")
        String refreshToken
) {}
