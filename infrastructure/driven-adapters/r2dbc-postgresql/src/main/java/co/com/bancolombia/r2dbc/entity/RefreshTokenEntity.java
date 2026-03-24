package co.com.bancolombia.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RefreshTokenEntity {
    @Id
    private String id;
    private String userId;
    private String token;
    private LocalDateTime expiraEn;
    private boolean revocado;
    private String dispositivo;
    private LocalDateTime creadoEn;
}
