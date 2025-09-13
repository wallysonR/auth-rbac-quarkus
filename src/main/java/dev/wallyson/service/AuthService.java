package dev.wallyson.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import dev.wallyson.api.dto.LoginRequest;
import dev.wallyson.repo.UserRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository users;

    public String authenticate(LoginRequest req) {
        var userOpt = users.findByUsername(req.username);
        if (userOpt.isEmpty()) return null;
        var user = userOpt.get();
        if (!user.enabled) return null;
        var result = BCrypt.verifyer().verify(req.password.toCharArray(), user.passwordHash);
        if (!result.verified) return null;
        Set<String> groups = user.roles.stream().map(r -> r.name).collect(Collectors.toSet());
        return Jwt.issuer("auth-rbac-quarkus").upn(user.username).groups(groups).expiresIn(Duration.ofHours(2)).sign();
    }
}
