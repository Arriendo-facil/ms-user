package co.com.bancolombia.api.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

public class CreateUserDto {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @URL
    private String urlPhoto;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String country;

    private boolean emailVerified;
}
