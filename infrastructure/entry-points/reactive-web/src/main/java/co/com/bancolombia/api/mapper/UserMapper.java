package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.CreateUserDto;
import co.com.bancolombia.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(CreateUserDto userDto);
}
