package co.com.bancolombia.r2dbc.mapper;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", implementationName = "R2dbcUserMapperImpl")
public interface UserMapper {

    @Mapping(source = "active", target = "isActive")
    User toUser(UserEntity entity);

    @Mapping(source = "active", target = "isActive")
    @Mapping(target = "newRecord", ignore = true)
    UserEntity toUserEntity(User user);
}
