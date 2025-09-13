package dev.wallyson.api.dto;

import java.util.Set;

public class LoginResponse {
    public String token;
    public String username;
    public Set<String> roles;

    public LoginResponse(String token, String username, Set<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }
}
