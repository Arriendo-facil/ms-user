package co.com.bancolombia.r2dbc.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import co.com.bancolombia.r2dbc.mapper.UserMapper;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserRepositoryAdapterTest {

    @Mock private UserReactiveRepository repository;
    @Mock private UserMapper mapper;

    private UserRepositoryAdapter adapter;

    private final UserEntity entity = UserEntity.builder()
            .id("user-1").email("user@test.com").fullName("Test User").build();

    private final User domain = User.builder()
            .id("user-1").email("user@test.com").fullName("Test User").build();

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(repository, mapper);
        when(mapper.toUserEntity(domain)).thenReturn(entity);
        when(mapper.toUser(entity)).thenReturn(domain);
    }

    // -------------------------------------------------------------------------
    // saveUser()
    // -------------------------------------------------------------------------

    @Test
    void saveUser_mapsToEntitySetsNewRecordAndSaves() {
        when(repository.save(any())).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.saveUser(domain))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(repository).save(any(UserEntity.class));
    }

    @Test
    void saveUser_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.save(any())).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(entity);
        });

        StepVerifier.create(adapter.saveUser(domain))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(repository, times(3)).save(any(UserEntity.class));
    }

    @Test
    void saveUser_doesNotRetryOnNonTransientException() {
        when(repository.save(any())).thenReturn(Mono.error(new RuntimeException("Constraint violation")));

        StepVerifier.create(adapter.saveUser(domain))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void saveUser_propagatesErrorAfterRetryExhausted() {
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};
        when(repository.save(any())).thenReturn(Mono.error(transientEx));

        StepVerifier.create(adapter.saveUser(domain))
                .expectError()
                .verify();

        verify(repository, times(3)).save(any(UserEntity.class));
    }

    // -------------------------------------------------------------------------
    // findById()
    // -------------------------------------------------------------------------

    @Test
    void findById_delegatesToRepositoryAndMaps() {
        when(repository.findById("user-1")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.findById("user-1"))
                .assertNext(result -> assertThat(result.getEmail()).isEqualTo("user@test.com"))
                .verifyComplete();
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        when(repository.findById("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("unknown"))
                .verifyComplete();
    }

    @Test
    void findById_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.findById("user-1")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(entity);
        });

        StepVerifier.create(adapter.findById("user-1"))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(repository, times(3)).findById("user-1");
    }

    @Test
    void findById_doesNotRetryOnNonTransientException() {
        when(repository.findById("user-1")).thenReturn(Mono.error(new RuntimeException("Query error")));

        StepVerifier.create(adapter.findById("user-1"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).findById("user-1");
    }

    // -------------------------------------------------------------------------
    // getByEmail()
    // -------------------------------------------------------------------------

    @Test
    void getByEmail_delegatesToRepositoryAndMaps() {
        when(repository.findByEmail("user@test.com")).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.getByEmail("user@test.com"))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();
    }

    @Test
    void getByEmail_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.findByEmail("user@test.com")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(entity);
        });

        StepVerifier.create(adapter.getByEmail("user@test.com"))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(repository, times(3)).findByEmail("user@test.com");
    }

    // -------------------------------------------------------------------------
    // updateUser()
    // -------------------------------------------------------------------------

    @Test
    void updateUser_savesEntityAndMaps() {
        when(repository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(adapter.updateUser(domain))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();
    }

    @Test
    void updateUser_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.save(entity)).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(entity);
        });

        StepVerifier.create(adapter.updateUser(domain))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(repository, times(3)).save(entity);
    }

    // -------------------------------------------------------------------------
    // desactivateUser()
    // -------------------------------------------------------------------------

    @Test
    void desactivateUser_whenUserExists_returnsTrue() {
        when(repository.deactivateByEmail("user@test.com")).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.desactivateUser("user@test.com"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();
    }

    @Test
    void desactivateUser_whenUserNotFound_returnsFalse() {
        when(repository.deactivateByEmail("noexiste@test.com")).thenReturn(Mono.just(0));

        StepVerifier.create(adapter.desactivateUser("noexiste@test.com"))
                .assertNext(result -> assertThat(result).isFalse())
                .verifyComplete();
    }

    @Test
    void desactivateUser_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.deactivateByEmail("user@test.com")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(1);
        });

        StepVerifier.create(adapter.desactivateUser("user@test.com"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();

        verify(repository, times(3)).deactivateByEmail("user@test.com");
    }

    // -------------------------------------------------------------------------
    // activateUser()
    // -------------------------------------------------------------------------

    @Test
    void activateUser_whenUserExists_returnsTrue() {
        when(repository.activateByEmail("user@test.com")).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.activateUser("user@test.com"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();
    }

    @Test
    void activateUser_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.activateByEmail("user@test.com")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.just(1);
        });

        StepVerifier.create(adapter.activateUser("user@test.com"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();

        verify(repository, times(3)).activateByEmail("user@test.com");
    }

    // -------------------------------------------------------------------------
    // deleteUser()
    // -------------------------------------------------------------------------

    @Test
    void deleteUser_delegatesToRepository() {
        when(repository.deleteByEmail("user@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteUser("user@test.com"))
                .verifyComplete();

        verify(repository).deleteByEmail("user@test.com");
    }

    @Test
    void deleteUser_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(repository.deleteByEmail("user@test.com")).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) return Mono.error(transientEx);
            return Mono.empty();
        });

        StepVerifier.create(adapter.deleteUser("user@test.com"))
                .verifyComplete();

        verify(repository, times(3)).deleteByEmail("user@test.com");
    }

    @Test
    void deleteUser_doesNotRetryOnNonTransientException() {
        when(repository.deleteByEmail("user@test.com"))
                .thenReturn(Mono.error(new RuntimeException("Constraint violation")));

        StepVerifier.create(adapter.deleteUser("user@test.com"))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).deleteByEmail("user@test.com");
    }
}
