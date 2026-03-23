package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error estándar")
public record ErrorResponse(
        @Schema(description = "Código semántico del error", example = "INVALID_CREDENTIALS")
        String errorCode,
        @Schema(description = "Mensaje descriptivo del error", example = "Credenciales incorrectas")
        String message
) {}
