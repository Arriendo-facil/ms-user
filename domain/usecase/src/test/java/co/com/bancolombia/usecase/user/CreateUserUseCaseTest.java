package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.exception.ConflictException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    void execute_whenEmailNotRegistered_createsUserWithDefaults() {
        User input = User.builder().email("nuevo@test.com").passwordHash("plain123").build();
        when(userRepository.getByEmail("nuevo@test.com")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("plain123")).thenReturn("hashed123");
        when(userRepository.saveUser(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.execute(input))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotNull();
                    assertThat(saved.getPasswordHash()).isEqualTo("hashed123");
                    assertThat(saved.isActive()).isTrue();
                    assertThat(saved.isEmailVerified()).isFalse();
                    assertThat(saved.getCreatedAt()).isNotNull();
                    assertThat(saved.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void execute_whenEmailAlreadyExists_throwsConflictException() {
        User existing = User.builder().email("existe@test.com").build();
        User input = User.builder().email("existe@test.com").build();
        when(userRepository.getByEmail("existe@test.com")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.execute(input))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ConflictException.class);
                    assertThat(((ConflictException) error).getErrorCode()).isEqualTo("USER_ALREADY_EXISTS");
                })
                .verify();
    }
}
