package dev.wallyson.api;

import dev.wallyson.api.dto.LoginRequest;
import dev.wallyson.api.dto.LoginResponse;
import dev.wallyson.repo.UserRepository;
import dev.wallyson.service.AuthService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.stream.Collectors;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService auth;

    @Inject
    UserRepository users;

    @Inject
    SecurityIdentity identity;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest req) {
        var token = auth.authenticate(req);
        if (token == null) return Response.status(Response.Status.UNAUTHORIZED).build();
        var u = users.findByUsername(req.username).orElseThrow();
        var roles = u.roles.stream().map(r -> r.name).collect(Collectors.toSet());
        return Response.ok(new LoginResponse(token, u.username, roles)).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"USER","ADMIN"})
    public Response me() {
        var name = identity.getPrincipal().getName();
        var roles = identity.getRoles();
        return Response.ok(name).build();
    }
}
