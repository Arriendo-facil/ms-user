package co.com.bancolombia.r2dbc.mapper;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserEntity entity);
    UserEntity toUserEntity(User user);
}
