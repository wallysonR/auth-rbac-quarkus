package dev.wallyson.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthResourceTest {

    @Test
    @DisplayName("Deve autenticar admin e retornar JWT")
    void shouldLoginAdminAndReturnJwt() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("username", equalTo("admin"))
                .body("roles", hasItems("ADMIN", "USER"));
    }

    @Test
    @DisplayName("Deve falhar com credenciais inválidas (401)")
    void shouldFailWithInvalidCredentials() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"wrong\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Com JWT válido do admin, deve acessar /user e /admin")
    void shouldAccessProtectedEndpointsWithValidToken() {
        String token =
                given()
                        .contentType(ContentType.JSON)
                        .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
                        .when()
                        .post("/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .path("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/user")
                .then()
                .statusCode(200)
                .body(equalTo("user-ok"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/admin")
                .then()
                .statusCode(200)
                .body(equalTo("admin-ok"));
    }

    @Test
    @DisplayName("Sem header Authorization deve retornar 401")
    void shouldRejectWithoutAuthorizationHeader() {
        given()
                .when()
                .get("/user")
                .then()
                .statusCode(401);
    }
}
