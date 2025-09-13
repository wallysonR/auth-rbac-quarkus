package dev.wallyson.repo;

import dev.wallyson.domain.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {
    public Optional<UserEntity> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }
}
