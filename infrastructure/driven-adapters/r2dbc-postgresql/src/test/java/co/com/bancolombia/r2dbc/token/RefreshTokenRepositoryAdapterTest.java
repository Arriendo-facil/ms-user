package co.com.bancolombia.r2dbc.token;

import co.com.bancolombia.model.auth.RefreshToken;
import co.com.bancolombia.r2dbc.entity.RefreshTokenEntity;
import co.com.bancolombia.r2dbc.mapper.RefreshTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.TransientDataAccessException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(mapper.toRefreshTokenEntity(domain)).thenReturn(entity);
        when(mapper.toRefreshToken(entity)).thenReturn(domain);
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Test
    void save_mapsToEntitySavesAndMapsBack() {
        when(repository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(domain))
                .assertNext(result -> assertThat(result.getToken()).isEqualTo("raw-token"))
                .verifyComplete();
    }

    @Test
    void save_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.save(entity)).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(entity);
        });

        StepVerifier.create(adapter.save(domain))
                .assertNext(result -> assertThat(result.getToken()).isEqualTo("raw-token"))
                .verifyComplete();

        verify(repository, times(3)).save(entity);
    }

    @Test
    void save_doesNotRetryOnNonTransientException() {
        when(repository.save(entity)).thenReturn(Mono.error(new RuntimeException("Constraint violation")));

        StepVerifier.create(adapter.save(domain))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).save(entity);
    }

    @Test
    void save_propagatesErrorAfterRetryExhausted() {
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};
        when(repository.save(entity)).thenReturn(Mono.error(transientEx));

        StepVerifier.create(adapter.save(domain))
                .expectError()
                .verify();

        verify(repository, times(3)).save(entity);
    }

    // -------------------------------------------------------------------------
    // findByToken()
    // -------------------------------------------------------------------------

    @Test
    void findByToken_delegatesToRepositoryAndMaps() {
        when(repository.findByToken("raw-token")).thenReturn(Mono.just(entity));

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
    void findByToken_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.findByToken("raw-token")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(entity);
        });

        StepVerifier.create(adapter.findByToken("raw-token"))
                .assertNext(result -> assertThat(result.getToken()).isEqualTo("raw-token"))
                .verifyComplete();

        verify(repository, times(3)).findByToken("raw-token");
    }

    @Test
    void findByToken_doesNotRetryOnNonTransientException() {
        when(repository.findByToken("raw-token")).thenReturn(Mono.error(new RuntimeException("Query error")));

        StepVerifier.create(adapter.findByToken("raw-token"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).findByToken("raw-token");
    }

    // -------------------------------------------------------------------------
    // revoke()
    // -------------------------------------------------------------------------

    @Test
    void revoke_delegatesToRepositoryAndCompletesEmpty() {
        when(repository.revokeByToken("raw-token")).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.revoke("raw-token"))
                .verifyComplete();

        verify(repository).revokeByToken("raw-token");
    }

    @Test
    void revoke_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.revokeByToken("raw-token")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(1);
        });

        StepVerifier.create(adapter.revoke("raw-token"))
                .verifyComplete();

        verify(repository, times(3)).revokeByToken("raw-token");
    }

    @Test
    void revoke_doesNotRetryOnNonTransientException() {
        when(repository.revokeByToken("raw-token")).thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(adapter.revoke("raw-token"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).revokeByToken("raw-token");
    }

    // -------------------------------------------------------------------------
    // revokeAllByUserId()
    // -------------------------------------------------------------------------

    @Test
    void revokeAllByUserId_delegatesToRepositoryAndCompletesEmpty() {
        when(repository.revokeAllByUserId("user-1")).thenReturn(Mono.just(3));

        StepVerifier.create(adapter.revokeAllByUserId("user-1"))
                .verifyComplete();

        verify(repository).revokeAllByUserId("user-1");
    }

    @Test
    void revokeAllByUserId_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.revokeAllByUserId("user-1")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(3);
        });

        StepVerifier.create(adapter.revokeAllByUserId("user-1"))
                .verifyComplete();

        verify(repository, times(3)).revokeAllByUserId("user-1");
    }

    @Test
    void revokeAllByUserId_doesNotRetryOnNonTransientException() {
        when(repository.revokeAllByUserId("user-1")).thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(adapter.revokeAllByUserId("user-1"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).revokeAllByUserId("user-1");
    }
}
