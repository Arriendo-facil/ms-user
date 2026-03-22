package co.com.bancolombia.api.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class UserRouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler userHandler) {
        return route(
                POST("api/user", )
        );
    }
}
