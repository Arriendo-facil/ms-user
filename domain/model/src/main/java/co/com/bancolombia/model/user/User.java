package co.com.bancolombia.model.user;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private String id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String urlPhoto;
    private String city;
    private String department;
    private String country;
    private boolean isActive;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
