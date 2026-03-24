package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.model.auth.gateways.PasswordResetRepository;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.PasswordEncoder;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetRepository passwordResetRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private ResetPasswordUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCase(userRepository, passwordResetRepository, passwordEncoder);
    }

    @Test
    void execute_withValidToken_updatesPasswordAndMarksTokenUsed() {
        User user = User.builder().id("user-1").email("user@test.com").passwordHash("old-hash").build();
        PasswordReset reset = PasswordReset.builder()
                .id("reset-1")
                .userId("user-1")
                .expiraEn(LocalDateTime.now().plusMinutes(30))
                .build();

        when(passwordResetRepository.findActiveByTokenHash(anyString())).thenReturn(Mono.just(reset));
        when(userRepository.findById("user-1")).thenReturn(Mono.just(user));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(userRepository.updateUser(any())).thenReturn(Mono.just(user));
        when(passwordResetRepository.markAsUsed(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("plain-token", "new-password"))
                .verifyComplete();
    }

    @Test
    void execute_withInvalidToken_throwsValidationException() {
        when(passwordResetRepository.findActiveByTokenHash(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("invalid-token", "new-password"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ValidationException.class);
                    assertThat(((ValidationException) error).getErrorCode()).isEqualTo("INVALID_RESET_TOKEN");
                })
                .verify();
    }

    @Test
    void execute_whenUserNotFound_throwsNotFoundException() {
        PasswordReset reset = PasswordReset.builder()
                .userId("user-1")
                .expiraEn(LocalDateTime.now().plusMinutes(30))
                .build();

        when(passwordResetRepository.findActiveByTokenHash(anyString())).thenReturn(Mono.just(reset));
        when(userRepository.findById("user-1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("plain-token", "new-password"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NotFoundException.class);
                    assertThat(((NotFoundException) error).getErrorCode()).isEqualTo("USER_NOT_FOUND");
                })
                .verify();
    }
}
