package dev.wallyson.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.wallyson.api.dto.LoginRequest;
import dev.wallyson.domain.RoleEntity;
import dev.wallyson.domain.UserEntity;
import dev.wallyson.repo.RoleRepository;
import dev.wallyson.repo.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthServiceTest {

    @Inject
    AuthService auth;

    @Inject
    UserRepository users;

    @Inject
    RoleRepository roles;

    @BeforeEach
    @Transactional
    void ensureData() {
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

        if (users.findByUsername("maria").isEmpty()) {
            var u = new UserEntity();
            u.username = "maria";
            u.passwordHash = BCrypt.withDefaults().hashToString(12, "maria123".toCharArray());
            u.enabled = false; // desabilitado
            u.roles = Set.of(userRole);
            users.persist(u);
        }
    }

    @Test
    @DisplayName("authenticate deve retornar JWT com credenciais válidas")
    void authenticate_shouldReturnJwt_forValidCredentials() {
        var req = new LoginRequest();
        req.username = "admin";
        req.password = "admin123";

        String token = auth.authenticate(req);
        assertNotNull(token, "Token não deveria ser nulo para credenciais válidas");

        var payload = jwtPayload(token);
        assertEquals("admin", payload.get("upn"), "upn deve ser o username");

        @SuppressWarnings("unchecked")
        List<String> groups = (List<String>) payload.getOrDefault("groups", List.of());
        assertTrue(groups.contains("ADMIN") && groups.contains("USER"), "Token deve conter roles ADMIN e USER");

        assertEquals("auth-rbac-quarkus", payload.get("iss"), "Issuer deve bater com a configuração");
        assertTrue(payload.containsKey("exp"), "Token deve conter expiração (exp)");
    }

    @Test
    @DisplayName("authenticate deve retornar null para senha incorreta")
    void authenticate_shouldReturnNull_forWrongPassword() {
        var req = new LoginRequest();
        req.username = "admin";
        req.password = "wrong";

        String token = auth.authenticate(req);
        assertNull(token, "Token deve ser nulo para senha incorreta");
    }

    @Test
    @DisplayName("authenticate deve retornar null para usuário inexistente")
    void authenticate_shouldReturnNull_forUnknownUser() {
        var req = new LoginRequest();
        req.username = "ghost";
        req.password = "whatever";

        String token = auth.authenticate(req);
        assertNull(token, "Token deve ser nulo para usuário inexistente");
    }

    @Test
    @DisplayName("authenticate deve retornar null para usuário desabilitado")
    void authenticate_shouldReturnNull_forDisabledUser() {
        var req = new LoginRequest();
        req.username = "maria";
        req.password = "maria123";

        String token = auth.authenticate(req);
        assertNull(token, "Token deve ser nulo para usuário desabilitado");
    }

    private Map<String, Object> jwtPayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) throw new IllegalArgumentException("JWT inválido");
            var json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return parseJsonToMap(json);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao decodificar payload do JWT", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {

        try {
            var clazz = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            var om = clazz.getDeclaredConstructor().newInstance();
            var readValue = clazz.getMethod("readValue", String.class, Class.class);
            return (Map<String, Object>) readValue.invoke(om, json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao parsear JSON do JWT", e);
        }
    }
}
