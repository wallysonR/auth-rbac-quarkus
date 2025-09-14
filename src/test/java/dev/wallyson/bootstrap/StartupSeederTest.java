package dev.wallyson.bootstrap;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.wallyson.domain.RoleEntity;
import dev.wallyson.domain.UserEntity;
import dev.wallyson.repo.RoleRepository;
import dev.wallyson.repo.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StartupSeederBranchCoverageTest {

    @Inject StartupSeeder seeder;
    @Inject RoleRepository roles;
    @Inject UserRepository users;

    @Transactional
    void clearAll() {
        users.deleteAll();
        roles.deleteAll();
    }

    @Test
    @DisplayName("Cenário A: DB vazio → cria ADMIN, USER e admin/admin123")
    void freshDb_createsAll() {
        clearAll();

        seeder.ensureDefaults();

        var adminRole = roles.findByName("ADMIN").orElse(null);
        var userRole  = roles.findByName("USER").orElse(null);
        assertNotNull(adminRole);
        assertNotNull(userRole);

        var admin = users.findByUsername("admin").orElse(null);
        assertNotNull(admin);
        assertTrue(admin.enabled);
        assertTrue(BCrypt.verifyer().verify("admin123".toCharArray(), admin.passwordHash).verified);

        var names = admin.roles.stream().map(r -> r.name).collect(Collectors.toSet());
        assertTrue(names.contains("ADMIN"));
        assertTrue(names.contains("USER"));
    }

    @Test
    @DisplayName("Cenário B: Roles já existem, mas não há admin → cria apenas admin")
    void rolesExist_noAdmin_createsOnlyAdmin() {
        clearAll();

        createRole("ADMIN");
        createRole("USER");
        assertTrue(users.findByUsername("admin").isEmpty(), "Não deveria existir admin antes do seed");

        seeder.ensureDefaults();

        assertEquals(1, roles.find("name", "ADMIN").list().size());
        assertEquals(1, roles.find("name", "USER").list().size());

        var admin = users.findByUsername("admin").orElse(null);
        assertNotNull(admin);
        assertTrue(BCrypt.verifyer().verify("admin123".toCharArray(), admin.passwordHash).verified);
    }

    @Test
    @DisplayName("Cenário C: Só USER existe → cria ADMIN e admin")
    void onlyUserRoleExists_createsAdminRole_and_AdminUser() {
        clearAll();

        createRole("USER");

        seeder.ensureDefaults();

        assertTrue(roles.findByName("ADMIN").isPresent(), "Role ADMIN deve ser criada");
        assertTrue(roles.findByName("USER").isPresent(),  "Role USER deve existir");

        assertTrue(users.findByUsername("admin").isPresent(), "Usuário admin deve ser criado");
    }

    @Test
    @DisplayName("Cenário D: Admin já existe → não duplica nada (idempotente)")
    void adminAlreadyExists_isIdempotent() {
        clearAll();

        var adminRole = createRole("ADMIN");
        var userRole  = createRole("USER");
        createUser("admin", "admin123", true, Set.of(adminRole, userRole));

        seeder.ensureDefaults(); // não deve criar nada novo

        assertEquals(1, roles.find("name", "ADMIN").list().size());
        assertEquals(1, roles.find("name", "USER").list().size());
        assertEquals(1, users.find("username", "admin").list().size());
    }


    @Transactional
    RoleEntity createRole(String name) {
        var r = new RoleEntity();
        r.name = name;
        roles.persist(r);
        return r;
    }

    @Transactional
    void createUser(String username, String rawPassword, boolean enabled, Set<RoleEntity> rs) {
        var u = new UserEntity();
        u.username = username;
        u.passwordHash = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        u.enabled = enabled;
        u.roles = rs;
        users.persist(u);
    }
}
