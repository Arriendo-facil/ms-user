package co.com.bancolombia.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserEntity implements Persistable<String> {
    @Id
    private String id;

    @Transient
    @Builder.Default
    private boolean newRecord = false;

    @Override
    public boolean isNew() {
        return newRecord;
    }
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
