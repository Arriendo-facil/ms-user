package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.user.CreateUserDto;
import co.com.bancolombia.api.dto.user.UserResponse;
import co.com.bancolombia.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "ApiUserMapperImpl")
public interface UserMapper {
    @Mapping(source = "password", target = "passwordHash")
    User toUser(CreateUserDto userDto);
    UserResponse toResponse(User user);
}
