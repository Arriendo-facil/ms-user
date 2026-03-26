package co.com.bancolombia.r2dbc.token;

import co.com.bancolombia.model.auth.PasswordReset;
import co.com.bancolombia.r2dbc.entity.PasswordResetEntity;
import co.com.bancolombia.r2dbc.mapper.PasswordResetMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(mapper.toPasswordResetEntity(domain)).thenReturn(entity);
        when(mapper.toPasswordReset(entity)).thenReturn(domain);
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Test
    void save_mapsToEntitySavesAndMapsBack() {
        when(repository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.save(domain))
                .assertNext(result -> {
                    assertThat(result.getUserId()).isEqualTo("user-1");
                    assertThat(result.getTokenHash()).isEqualTo("hash-abc");
                })
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
                .assertNext(result -> assertThat(result.getUserId()).isEqualTo("user-1"))
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
    // findActiveByTokenHash()
    // -------------------------------------------------------------------------

    @Test
    void findActiveByTokenHash_delegatesToRepositoryWithCurrentTimeAndMaps() {
        when(repository.findActiveByTokenHash(eq("hash-abc"), any(LocalDateTime.class)))
                .thenReturn(Mono.just(entity));

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
    void findActiveByTokenHash_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.findActiveByTokenHash(eq("hash-abc"), any(LocalDateTime.class))).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(entity);
        });

        StepVerifier.create(adapter.findActiveByTokenHash("hash-abc"))
                .assertNext(result -> assertThat(result.getTokenHash()).isEqualTo("hash-abc"))
                .verifyComplete();

        verify(repository, times(3)).findActiveByTokenHash(eq("hash-abc"), any(LocalDateTime.class));
    }

    @Test
    void findActiveByTokenHash_doesNotRetryOnNonTransientException() {
        when(repository.findActiveByTokenHash(eq("hash-abc"), any(LocalDateTime.class)))
                .thenReturn(Mono.error(new RuntimeException("Query error")));

        StepVerifier.create(adapter.findActiveByTokenHash("hash-abc"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).findActiveByTokenHash(eq("hash-abc"), any(LocalDateTime.class));
    }

    // -------------------------------------------------------------------------
    // markAsUsed()
    // -------------------------------------------------------------------------

    @Test
    void markAsUsed_delegatesToRepositoryAndCompletesEmpty() {
        when(repository.markAsUsed("hash-abc")).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.markAsUsed("hash-abc"))
                .verifyComplete();

        verify(repository).markAsUsed("hash-abc");
    }

    @Test
    void markAsUsed_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.markAsUsed("hash-abc")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(1);
        });

        StepVerifier.create(adapter.markAsUsed("hash-abc"))
                .verifyComplete();

        verify(repository, times(3)).markAsUsed("hash-abc");
    }

    @Test
    void markAsUsed_doesNotRetryOnNonTransientException() {
        when(repository.markAsUsed("hash-abc")).thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(adapter.markAsUsed("hash-abc"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).markAsUsed("hash-abc");
    }
}
