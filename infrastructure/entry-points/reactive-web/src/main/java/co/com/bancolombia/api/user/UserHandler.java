package co.com.bancolombia.api.user;

import co.com.bancolombia.api.dto.CreateUserDto;
import co.com.bancolombia.api.mapper.UserMapper;
import co.com.bancolombia.usecase.user.CreateUserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserHandler {
    private final Validator validator;
    private final CreateUserUseCase createUserUseCase;
    private final UserMapper mapper;

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserDto.class)
                .doOnNext(this::validate)
                .map(mapper::toUser)
                .flatMap(createUserUseCase::execute)
                .flatMap(user -> ServerResponse.status(HttpStatus.CREATED).bodyValue(user));
    }

    private void validate(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
