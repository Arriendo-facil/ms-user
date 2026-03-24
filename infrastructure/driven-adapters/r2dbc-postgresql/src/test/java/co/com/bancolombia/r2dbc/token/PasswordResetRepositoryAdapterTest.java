package co.com.bancolombia.r2dbc.token;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.r2dbc.entity.PasswordResetEntity;
import co.com.bancolombia.r2dbc.mapper.PasswordResetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetRepositoryAdapterTest {

    @Mock private PasswordResetReactiveRepository repository;
    @Mock private PasswordResetMapper mapper;

    private PasswordResetRepositoryAdapter adapter;

    private final PasswordResetEntity entity = PasswordResetEntity.builder()
            .id("reset-1").userId("user-1").tokenHash("hash-abc")
            .expiraEn(LocalDateTime.now().plusMinutes(30)).usado(false).build();

    private final PasswordReset domain = PasswordReset.builder()
            .id("reset-1").userId("user-1").tokenHash("hash-abc")
            .expiraEn(LocalDateTime.now().plusMinutes(30)).usado(false).build();

    @BeforeEach
    void setUp() {
        adapter = new PasswordResetRepositoryAdapter(repository, mapper);
    }

    @Test
    void save_mapsToEntitySavesAndMapsBack() {
        when(mapper.toPasswordResetEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toPasswordReset(entity)).thenReturn(domain);

        StepVerifier.create(adapter.save(domain))
                .assertNext(result -> {
                    assertThat(result.getUserId()).isEqualTo("user-1");
                    assertThat(result.getTokenHash()).isEqualTo("hash-abc");
                })
                .verifyComplete();
    }

    @Test
    void findActiveByTokenHash_delegatesToRepositoryWithCurrentTimeAndMaps() {
        when(repository.findActiveByTokenHash(eq("hash-abc"), any(LocalDateTime.class)))
                .thenReturn(Mono.just(entity));
        when(mapper.toPasswordReset(entity)).thenReturn(domain);

        StepVerifier.create(adapter.findActiveByTokenHash("hash-abc"))
                .assertNext(result -> assertThat(result.getUserId()).isEqualTo("user-1"))
                .verifyComplete();
    }

    @Test
    void findActiveByTokenHash_whenNotFound_returnsEmpty() {
        when(repository.findActiveByTokenHash(eq("expired-hash"), any(LocalDateTime.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(adapter.findActiveByTokenHash("expired-hash"))
                .verifyComplete();
    }

    @Test
    void markAsUsed_delegatesToRepositoryAndCompletesEmpty() {
        when(repository.markAsUsed("hash-abc")).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.markAsUsed("hash-abc"))
                .verifyComplete();

        verify(repository).markAsUsed("hash-abc");
    }
}
