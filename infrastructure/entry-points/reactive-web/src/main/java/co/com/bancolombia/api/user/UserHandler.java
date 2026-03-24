package co.com.bancolombia.api.user;

import co.com.bancolombia.api.dto.user.CreateUserDto;
import co.com.bancolombia.api.mapper.UserMapper;
import co.com.bancolombia.usecase.user.CreateUserUseCase;
import co.com.bancolombia.usecase.user.GetUserUseCase;
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
    private final GetUserUseCase getUserUseCase;
    private final UserMapper mapper;

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserDto.class)
                .flatMap(dto -> {
                    Set<ConstraintViolation<Object>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        return Mono.error(new ConstraintViolationException(violations));
                    }
                    return Mono.just(dto);
                })
                .map(mapper::toUser)
                .flatMap(createUserUseCase::execute)
                .flatMap(user -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(mapper.toResponse(user))
                );
    }

    public Mono<ServerResponse> getUser(ServerRequest request) {
        String id = request.pathVariable("id");

        return getUserUseCase.execute(id)
                .flatMap(user ->  ServerResponse
                        .ok()
                        .bodyValue(mapper.toResponse(user))
                );
    }
}
