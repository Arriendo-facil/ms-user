package co.com.bancolombia.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private String id;
    private String userId;
    private String token;
    private LocalDateTime expiraEn;
    private boolean revocado;
    private String dispositivo;
    private LocalDateTime creadoEn;
}
