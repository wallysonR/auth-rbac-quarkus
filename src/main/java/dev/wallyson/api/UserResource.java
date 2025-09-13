package dev.wallyson.api;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/user")
public class UserResource {
    @GET
    @RolesAllowed({"USER","ADMIN"})
    public Response userArea() {
        return Response.ok("user-ok").build();
    }
}
