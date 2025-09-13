package dev.wallyson.bootstrap;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.wallyson.domain.RoleEntity;
import dev.wallyson.domain.UserEntity;
import dev.wallyson.repo.RoleRepository;
import dev.wallyson.repo.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.runtime.StartupEvent;

import java.util.Set;

@ApplicationScoped
public class StartupSeeder {

    @Inject
    RoleRepository roles;

    @Inject
    UserRepository users;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        var adminRole = roles.findByName("ADMIN").orElseGet(() -> {
            var r = new RoleEntity();
            r.name = "ADMIN";
            roles.persist(r);
            return r;
        });

        var userRole = roles.findByName("USER").orElseGet(() -> {
            var r = new RoleEntity();
            r.name = "USER";
            roles.persist(r);
            return r;
        });

        if (users.findByUsername("admin").isEmpty()) {
            var u = new UserEntity();
            u.username = "admin";
            u.passwordHash = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
            u.enabled = true;
            u.roles = Set.of(adminRole, userRole);
            users.persist(u);
        }
        if (users.findByUsername("jose").isEmpty()) {
            var u = new UserEntity();
            u.username = "jose";
            u.passwordHash = BCrypt.withDefaults().hashToString(12, "jose123".toCharArray());
            u.enabled = true;
            u.roles = Set.of(userRole); // só USER
            users.persist(u);
        }
    }
}
