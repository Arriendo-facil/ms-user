package co.com.bancolombia.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta del sistema cuando solicitan la informacion de un usuario desde un servicio publico")
public record UserResponse (
        @Schema(
                description = "Id del usuario",
                example = "1234-asda-3433",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String id,
        @Schema(
                description = "Nombre completo del usuario",
                example = "María García López",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String fullName,
        @Schema(
            description = "URL de la foto de perfil del usuario (opcional)",
            example = "https://storage.ejemplo.com/fotos/maria-garcia.jpg",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        String urlPhoto,
        @Schema(
                description = "Ciudad de residencia del usuario (opcional)",
                example = "Medellín",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String city,
        @Schema(
                description = "Departamento o estado de residencia del usuario (opcional)",
                example = "Antioquia",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String department,
        @Schema(
                description = "País de residencia del usuario (opcional)",
                example = "Colombia",
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String country,
        @Schema(
                description = "Indica si el email ya fue verificado. Normalmente false al registrarse.",
                example = "false",
                defaultValue = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean emailVerified
) {}
