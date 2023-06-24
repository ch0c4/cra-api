package org.johan.cra.controllers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import javax.sql.DataSource;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.johan.cra.domains.requests.RegisterRequest;
import org.johan.cra.domains.responses.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest
class UserControllerTest extends AbstractInitH2Database {

  private final EmbeddedServer embeddedServer;

  public UserControllerTest(DataSource dataSource, EmbeddedServer embeddedServer) {
    super(dataSource);
    this.embeddedServer = embeddedServer;
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = embeddedServer.getPort();
    super.executeInitSQL("init_script.sql");
  }

  @Test
  void test_register() {
    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new RegisterRequest("toto@gmail.com", "azeqsd"))
        .post("/users/register")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  void test_register_badRequest() {
    given()
        .when()
        .contentType(ContentType.JSON)
        .body(new RegisterRequest("default@gmail.com", "azeqsd"))
        .post("/users/register")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void test_login() {
    given()
        .when()
        .contentType(ContentType.JSON)
        .body("""
                { "username": "default@gmail.com", "password": "azeqsd" }""")
        .post("/users/login")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  void test_all() {
    var accessToken = getAccessTokenForAdmin();

    var actual =
        given()
            .when()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(accessToken)
            .get("/users")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(UserResponse[].class);

    assertNotNull(actual);
    assertEquals(1, actual.length);
    assertEquals(1L, actual[0].getId());
  }

  String getAccessTokenForAdmin() {
    var response =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body("""
                { "username": "admin@gmail.com", "password": "azeqsd" }""")
            .post("/users/login")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(LoginResponse.class);
    return response.access_token();
  }
}
