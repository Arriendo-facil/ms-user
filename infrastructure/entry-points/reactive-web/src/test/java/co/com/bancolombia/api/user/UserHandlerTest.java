package co.com.bancolombia.api.user;

import co.com.bancolombia.api.dto.user.CreateUserDto;
import co.com.bancolombia.api.dto.user.UserResponse;
import co.com.bancolombia.api.mapper.UserMapper;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.usecase.user.CreateUserUseCase;
import co.com.bancolombia.usecase.user.GetUserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock private Validator validator;
    @Mock private CreateUserUseCase createUserUseCase;
    @Mock private GetUserUseCase getUserUseCase;
    @Mock private UserMapper mapper;
    @Mock private ServerRequest serverRequest;

    private UserHandler handler;

    private final User user = User.builder()
            .id("user-1").fullName("María García").email("maria@test.com").build();

    private final UserResponse userResponse = new UserResponse(
            "user-1", "María García",null, "Medellín", "Antioquia",
            "Colombia", false);

    @BeforeEach
    void setUp() {
        handler = new UserHandler(validator, createUserUseCase, getUserUseCase, mapper);
        lenient().when(validator.validate(any())).thenReturn(Set.of());
    }

    @Test
    void createUser_withValidData_returns201WithUserResponse() {
        CreateUserDto dto = new CreateUserDto();
        dto.setFullName("María García");
        dto.setEmail("maria@test.com");
        dto.setPassword("MiPassw0rd!");

        when(serverRequest.bodyToMono(CreateUserDto.class)).thenReturn(Mono.just(dto));
        when(mapper.toUser(dto)).thenReturn(user);
        when(createUserUseCase.execute(user)).thenReturn(Mono.just(user));
        when(mapper.toResponse(user)).thenReturn(userResponse);

        StepVerifier.create(handler.createUser(serverRequest))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED))
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void createUser_withInvalidData_throwsConstraintViolationException() {
        CreateUserDto dto = new CreateUserDto();
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);

        when(serverRequest.bodyToMono(CreateUserDto.class)).thenReturn(Mono.just(dto));
        when(validator.validate(any())).thenReturn(Set.of(violation));

        StepVerifier.create(handler.createUser(serverRequest))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void getUser_whenUserExists_returns200WithUserResponse() {
        when(serverRequest.pathVariable("id")).thenReturn("user-1");
        when(getUserUseCase.execute("user-1")).thenReturn(Mono.just(user));
        when(mapper.toResponse(user)).thenReturn(userResponse);

        StepVerifier.create(handler.getUser(serverRequest))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }

    @Test
    void getUser_whenUserNotFound_propagatesError() {
        when(serverRequest.pathVariable("id")).thenReturn("unknown");
        when(getUserUseCase.execute("unknown")).thenReturn(Mono.error(
                new co.com.bancolombia.model.exception.NotFoundException("USER_NOT_FOUND", "No encontrado")));

        StepVerifier.create(handler.getUser(serverRequest))
                .expectError(co.com.bancolombia.model.exception.NotFoundException.class)
                .verify();
    }
}
