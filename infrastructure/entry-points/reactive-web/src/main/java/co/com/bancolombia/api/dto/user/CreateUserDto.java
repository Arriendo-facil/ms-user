package co.com.bancolombia.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos requeridos para registrar un nuevo usuario en el sistema")
public class CreateUserDto {

    @NotBlank
    @Schema(
        description = "Nombre completo del usuario",
        example = "María García López",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @NotBlank
    @Email
    @Schema(
        description = "Dirección de email única del usuario. Se usará para autenticación y notificaciones.",
        example = "maria.garcia@ejemplo.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(
        description = "Contraseña en texto plano. Debe tener entre 8 y 100 caracteres. Se almacena hasheada.",
        example = "MiPassw0rd!",
        minLength = 8,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @URL
    @Schema(
        description = "URL de la foto de perfil del usuario (opcional)",
        example = "https://storage.ejemplo.com/fotos/maria-garcia.jpg",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String urlPhoto;

    @Size(max = 100)
    @Schema(
        description = "Ciudad de residencia del usuario (opcional)",
        example = "Medellín",
        maxLength = 100,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String city;

    @Size(max = 100)
    @Schema(
        description = "Departamento o estado de residencia del usuario (opcional)",
        example = "Antioquia",
        maxLength = 100,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String department;

    @Size(max = 100)
    @Schema(
        description = "País de residencia del usuario (opcional)",
        example = "Colombia",
        maxLength = 100,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String country;
}
