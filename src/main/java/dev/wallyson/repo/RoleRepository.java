package dev.wallyson.repo;

import dev.wallyson.domain.RoleEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<RoleEntity> {
    public Optional<RoleEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
