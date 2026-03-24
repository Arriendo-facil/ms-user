package co.com.bancolombia.r2dbc.token;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.r2dbc.entity.RefreshTokenEntity;
import co.com.bancolombia.r2dbc.mapper.RefreshTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryAdapterTest {

    @Mock private RefreshTokenReactiveRepository repository;
    @Mock private RefreshTokenMapper mapper;

    private RefreshTokenRepositoryAdapter adapter;

    private final RefreshTokenEntity entity = RefreshTokenEntity.builder()
            .id("token-1").token("raw-token").userId("user-1")
            .expiraEn(LocalDateTime.now().plusDays(7)).build();

    private final RefreshToken domain = RefreshToken.builder()
            .id("token-1").token("raw-token").userId("user-1")
            .expiraEn(LocalDateTime.now().plusDays(7)).build();

    @BeforeEach
    void setUp() {
        adapter = new RefreshTokenRepositoryAdapter(repository, mapper);
    }

    @Test
    void save_mapsToEntitySavesAndMapsBack() {
        when(mapper.toRefreshTokenEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toRefreshToken(entity)).thenReturn(domain);

        StepVerifier.create(adapter.save(domain))
                .assertNext(result -> assertThat(result.getToken()).isEqualTo("raw-token"))
                .verifyComplete();
    }

    @Test
    void findByToken_delegatesToRepositoryAndMaps() {
        when(repository.findByToken("raw-token")).thenReturn(Mono.just(entity));
        when(mapper.toRefreshToken(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findByToken("raw-token"))
                .assertNext(result -> assertThat(result.getUserId()).isEqualTo("user-1"))
                .verifyComplete();
    }

    @Test
    void findByToken_whenNotFound_returnsEmpty() {
        when(repository.findByToken("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByToken("unknown"))
                .verifyComplete();
    }

    @Test
    void revoke_delegatesToRepositoryAndCompletesEmpty() {
        when(repository.revokeByToken("raw-token")).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.revoke("raw-token"))
                .verifyComplete();

        verify(repository).revokeByToken("raw-token");
    }

    @Test
    void revokeAllByUserId_delegatesToRepositoryAndCompletesEmpty() {
        when(repository.revokeAllByUserId("user-1")).thenReturn(Mono.just(3));

        StepVerifier.create(adapter.revokeAllByUserId("user-1"))
                .verifyComplete();

        verify(repository).revokeAllByUserId("user-1");
    }
}
