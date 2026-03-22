package co.com.bancolombia.api.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthRouterRest {

    @Bean
    public RouterFunction<ServerResponse> authRouter(AuthHandler authHandler) {
        return route(POST("/api/auth/login"), authHandler::login)
                .andRoute(POST("/api/auth/refresh"), authHandler::refresh)
                .andRoute(POST("/api/auth/logout"), authHandler::logout)
                .andRoute(POST("/api/auth/password-reset/request"), authHandler::requestPasswordReset)
                .andRoute(POST("/api/auth/password-reset/confirm"), authHandler::resetPassword);
    }
}
