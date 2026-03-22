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
public class PasswordReset {
    private String id;
    private String userId;
    private String tokenHash;
    private LocalDateTime expiraEn;
    private boolean usado;
    private LocalDateTime creadoEn;
}
