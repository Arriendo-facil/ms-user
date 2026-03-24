package co.com.bancolombia.r2dbc.mapper;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.r2dbc.entity.PasswordResetEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PasswordResetMapper {
    PasswordReset toPasswordReset(PasswordResetEntity entity);
    PasswordResetEntity toPasswordResetEntity(PasswordReset reset);
}
