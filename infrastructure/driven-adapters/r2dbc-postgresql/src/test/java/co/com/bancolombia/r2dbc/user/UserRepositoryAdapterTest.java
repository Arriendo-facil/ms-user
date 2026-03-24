package co.com.bancolombia.r2dbc.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import co.com.bancolombia.r2dbc.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    }

    @Test
    void saveUser_mapsToEntitySetsNewRecordAndSaves() {
        when(mapper.toUserEntity(domain)).thenReturn(entity);
        when(repository.save(any())).thenReturn(Mono.just(entity));
        when(mapper.toUser(entity)).thenReturn(domain);

        StepVerifier.create(adapter.saveUser(domain))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(repository).save(any(UserEntity.class));
    }

    @Test
    void findById_delegatesToRepositoryAndMaps() {
        when(repository.findById("user-1")).thenReturn(Mono.just(entity));
        when(mapper.toUser(entity)).thenReturn(domain);

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
    void getByEmail_delegatesToRepositoryAndMaps() {
        when(repository.findByEmail("user@test.com")).thenReturn(Mono.just(entity));
        when(mapper.toUser(entity)).thenReturn(domain);

        StepVerifier.create(adapter.getByEmail("user@test.com"))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();
    }

    @Test
    void updateUser_savesEntityAndMaps() {
        when(mapper.toUserEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.just(entity));
        when(mapper.toUser(entity)).thenReturn(domain);

        StepVerifier.create(adapter.updateUser(domain))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("user-1"))
                .verifyComplete();
    }

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
    void activateUser_whenUserExists_returnsTrue() {
        when(repository.activateByEmail("user@test.com")).thenReturn(Mono.just(1));

        StepVerifier.create(adapter.activateUser("user@test.com"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();
    }

    @Test
    void deleteUser_delegatesToRepository() {
        when(repository.deleteByEmail("user@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteUser("user@test.com"))
                .verifyComplete();

        verify(repository).deleteByEmail("user@test.com");
    }
}
