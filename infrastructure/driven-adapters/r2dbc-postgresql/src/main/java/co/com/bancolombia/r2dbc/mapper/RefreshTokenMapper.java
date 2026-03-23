package co.com.bancolombia.r2dbc.mapper;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.r2dbc.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    RefreshToken toRefreshToken(RefreshTokenEntity entity);
    RefreshTokenEntity toRefreshTokenEntity(RefreshToken token);
}
