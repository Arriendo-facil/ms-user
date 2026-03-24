package co.com.bancolombia.usecase.security;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.model.auth.gateways.PasswordResetRepository;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.user.User;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetRepository passwordResetRepository;

    private RequestPasswordResetUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RequestPasswordResetUseCase(userRepository, passwordResetRepository);
    }

    @Test
    void execute_whenEmailExists_savesResetAndReturnsToken() {
        User user = User.builder().id("user-1").email("user@test.com").build();
        PasswordReset saved = PasswordReset.builder()
                .id("reset-1")
                .userId("user-1")
                .expiraEn(LocalDateTime.now().plusMinutes(30))
                .build();

        when(userRepository.getByEmail("user@test.com")).thenReturn(Mono.just(user));
        when(passwordResetRepository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("user@test.com"))
                .assertNext(token -> assertThat(token).isNotBlank())
                .verifyComplete();
    }

    @Test
    void execute_whenEmailNotFound_throwsNotFoundException() {
        when(userRepository.getByEmail("noexiste@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("noexiste@test.com"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NotFoundException.class);
                    assertThat(((NotFoundException) error).getErrorCode()).isEqualTo("EMAIL_NOT_FOUND");
                })
                .verify();
    }
}
