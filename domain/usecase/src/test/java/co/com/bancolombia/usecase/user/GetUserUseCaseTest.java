package co.com.bancolombia.usecase.user;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserUseCase(userRepository);
    }

    @Test
    void execute_whenUserExists_returnsUser() {
        User user = User.builder().id("123").fullName("María García").email("maria@test.com").build();
        when(userRepository.findById("123")).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.execute("123"))
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo("123");
                    assertThat(result.getEmail()).isEqualTo("maria@test.com");
                })
                .verifyComplete();
    }

    @Test
    void execute_whenUserNotFound_throwsNotFoundException() {
        when(userRepository.findById("999")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("999"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NotFoundException.class);
                    assertThat(((NotFoundException) error).getErrorCode()).isEqualTo("USER_NOT_FOUND");
                })
                .verify();
    }
}
