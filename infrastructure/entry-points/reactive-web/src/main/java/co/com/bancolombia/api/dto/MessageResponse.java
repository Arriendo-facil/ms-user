package co.com.bancolombia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con mensaje informativo")
public record MessageResponse(
        @Schema(description = "Mensaje legible para el usuario", example = "Contraseña actualizada correctamente")
        String message
) {}
