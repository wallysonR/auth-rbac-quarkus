package dev.wallyson.bootstrap;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.wallyson.domain.RoleEntity;
import dev.wallyson.domain.UserEntity;
import dev.wallyson.repo.RoleRepository;
import dev.wallyson.repo.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StartupSeederTest {

    @Inject
    RoleRepository roles;

    @Inject
    UserRepository users;

    @Test
    @DisplayName("Seeder deve criar roles ADMIN e USER e o usuário admin/admin123 habilitado")
    void shouldCreateDefaultRolesAndAdmin() {
        var adminRole = roles.findByName("ADMIN").orElse(null);
        var userRole  = roles.findByName("USER").orElse(null);

        assertNotNull(adminRole, "Role ADMIN deve existir");
        assertNotNull(userRole,  "Role USER deve existir");

        var adminOpt = users.findByUsername("admin");
        assertTrue(adminOpt.isPresent(), "Usuário admin deve existir");

        UserEntity admin = adminOpt.get();
        assertNotNull(admin.id, "Admin deve ter ID gerado");
        assertTrue(admin.enabled, "Admin deve estar habilitado");
        assertNotNull(admin.passwordHash, "Senha do admin deve estar hashada");

        var verified = BCrypt.verifyer()
                .verify("admin123".toCharArray(), admin.passwordHash);
        assertTrue(verified.verified, "Senha do admin deve ser 'admin123' (hash confere)");

        Set<String> adminRoles = admin.roles.stream().map(r -> r.name).collect(java.util.stream.Collectors.toSet());
        assertTrue(adminRoles.contains("ADMIN"), "Admin deve possuir role ADMIN");
        assertTrue(adminRoles.contains("USER"),  "Admin deve possuir role USER");
    }

    @Test
    @DisplayName("Seeder deve ser idempotente: não duplica roles nem admin")
    void shouldBeIdempotent() {

        var adminByName = roles.find("name", "ADMIN").list();
        var userByName  = roles.find("name", "USER").list();
        assertEquals(1, adminByName.size(), "Deve existir exatamente 1 role ADMIN");
        assertEquals(1, userByName.size(),  "Deve existir exatamente 1 role USER");

        var admins = users.find("username", "admin").list();
        assertEquals(1, admins.size(), "Deve existir exatamente 1 usuário admin");
    }
}
