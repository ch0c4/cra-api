package org.johan.cra.controllers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.http.HttpStatus;
import org.johan.cra.clients.DayOffApi;
import org.johan.cra.domains.clients.DayOffApiResponse;
import org.johan.cra.domains.responses.CraResponse;
import org.johan.cra.helpers.TimeHelper;
import org.johan.cra.services.DayOffCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest
class CraControllerTest extends AbstractInitH2Database {

  @MockBean(TimeHelper.class)
  TimeHelper timeHelper() {
    return mock(TimeHelper.class);
  }

  private final TimeHelper timeHelper;

  @MockBean(DayOffCacheService.class)
  DayOffCacheService dayOffCacheService() {
    return mock(DayOffCacheService.class);
  }

  private final DayOffCacheService dayOffCacheService;

  private final EmbeddedServer embeddedServer;

  public CraControllerTest(
      DataSource dataSource,
      TimeHelper timeHelper,
      DayOffCacheService dayOffCacheService,
      EmbeddedServer embeddedServer) {
    super(dataSource);
    this.timeHelper = timeHelper;
    this.dayOffCacheService = dayOffCacheService;
    this.embeddedServer = embeddedServer;
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = embeddedServer.getPort();
    super.executeInitSQL("init_script.sql");
    when(timeHelper.now()).thenReturn(LocalDate.of(2023, 6, 22));
    when(dayOffCacheService.fetchDayOffForYear(2023))
        .thenReturn(List.of(new DayOffApiResponse(LocalDate.of(2023, 5, 1), "1er mai")));
  }

  @Test
  void test_getCurrentMonth() {
    var accessToken = getAccessTokenForDefault();

    var actual =
        given()
            .when()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(accessToken)
            .get("/cras")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(CraResponse[].class);

    assertEquals(1, actual.length);
    assertEquals("toto", actual[0].getProject());
  }

  @Test
  void test_getSpecificMonth() {
    var accessToken = getAccessTokenForDefault();

    var actual =
        given()
            .when()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(accessToken)
            .get("/cras/month/2")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(CraResponse[].class);

    assertEquals(1, actual.length);
    assertNull(actual[0].getProject());
  }

  @Test
  void test_currentMonthForUser() {
    var accessToken = getAccessTokenForAdmin();
    var actual =
        given()
            .when()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(accessToken)
            .get("/cras/user/1")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(CraResponse[].class);

    assertEquals(1, actual.length);
    assertEquals("toto", actual[0].getProject());
  }

  @Test
  void test_userNotFound() {
    var accessToken = getAccessTokenForAdmin();
    given()
        .when()
        .contentType(ContentType.JSON)
        .auth()
        .oauth2(accessToken)
        .get("/cras/user/9999")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void test_specificMonthForUser() {
    var accessToken = getAccessTokenForAdmin();
    var actual =
        given()
            .when()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(accessToken)
            .get("/cras/month/1/user/1")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(CraResponse[].class);

    assertEquals(1, actual.length);
    assertNull(actual[0].getProject());
  }

  @Test
  void test_upsertDay_insert() {
    var accessToken = getAccessTokenForDefault();
    given()
        .when()
        .contentType(ContentType.JSON)
        .auth()
        .oauth2(accessToken)
        .body("""
                {"project":  "toto", "craDate": "2023-06-23", "value": 1.0}""")
        .post("/cras")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    var actual =
        given()
            .when()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(accessToken)
            .get("/cras")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(CraResponse[].class);

    assertNotNull(actual);

    var actualList = List.of(actual);

    var optionalActualForProject =
        actualList.stream().filter(a -> a.getProject().equals("toto")).findFirst();
    assertTrue(optionalActualForProject.isPresent());

    var optionalActualForDay =
        optionalActualForProject.get().getDayElements().stream()
            .filter(a -> a.getDay().equals("2023-06-23"))
            .findFirst();
    assertTrue(optionalActualForDay.isPresent());
    assertEquals(1f, optionalActualForDay.get().getValue());
  }

  @Test
  void test_upsertDay_update() {
    var accessToken = getAccessTokenForDefault();
    given()
        .when()
        .contentType(ContentType.JSON)
        .auth()
        .oauth2(accessToken)
        .body("""
                {"project":  "toto", "craDate": "2023-06-22", "value": 0.5}""")
        .post("/cras")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    var actual =
        given()
            .when()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(accessToken)
            .get("/cras")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(CraResponse[].class);

    assertNotNull(actual);

    var actualList = List.of(actual);

    var optionalActualForProject =
        actualList.stream().filter(a -> a.getProject().equals("toto")).findFirst();
    assertTrue(optionalActualForProject.isPresent());

    var optionalActualForDay =
        optionalActualForProject.get().getDayElements().stream()
            .filter(a -> a.getDay().equals("2023-06-22"))
            .findFirst();
    assertTrue(optionalActualForDay.isPresent());
    assertEquals(.5f, optionalActualForDay.get().getValue());
  }

  @Test
  void test_upsertDay_badRequest_badValue() {
    var accessToken = getAccessTokenForDefault();
    given()
        .when()
        .contentType(ContentType.JSON)
        .auth()
        .oauth2(accessToken)
        .body("""
                {"project":  "toto", "craDate": "2023-06-22", "value": 1.5}""")
        .post("/cras")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void test_upsertDay_badRequest_weekend() {
    var accessToken = getAccessTokenForDefault();
    given()
        .when()
        .contentType(ContentType.JSON)
        .auth()
        .oauth2(accessToken)
        .body("""
                {"project":  "toto", "craDate": "2023-06-24", "value": 1.0}""")
        .post("/cras")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  void test_upsertDay_badRequest_dayOff() {
    var accessToken = getAccessTokenForDefault();
    given()
        .when()
        .contentType(ContentType.JSON)
        .auth()
        .oauth2(accessToken)
        .body("""
                {"project":  "toto", "craDate": "2023-05-01", "value": 1.0}""")
        .post("/cras")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  String getAccessTokenForDefault() {
    var response =
        given()
            .when()
            .contentType(ContentType.JSON)
            .body("""
                { "username": "default@gmail.com", "password": "azeqsd" }""")
            .post("/users/login")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .as(LoginResponse.class);
    return response.access_token();
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
