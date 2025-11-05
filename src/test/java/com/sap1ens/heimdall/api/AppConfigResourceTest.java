package com.sap1ens.heimdall.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import com.sap1ens.heimdall.AppConfig;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class AppConfigResourceTest {

  @InjectMock
  @MockitoConfig(returnsDeepMocks = true)
  AppConfig appConfig;

  @Test
  public void testConfigEndpointReturnsCorrectStructure() {
    Mockito.when(appConfig.patterns()).thenReturn(Map.of("display-name", "$jobName"));
    Mockito.when(appConfig.endpointPathPatterns())
        .thenReturn(
            Map.of(
                "flink-ui", "http://localhost/$jobName/ui",
                "flink-api", "http://localhost/$jobName/api"));

    given()
        .when()
        .get("/config")
        .then()
        .statusCode(200)
        .body("appVersion", notNullValue())
        .body("patterns.display-name", is("$jobName"))
        .body("endpointPathPatterns.flink-ui", is("http://localhost/$jobName/ui"))
        .body("endpointPathPatterns.flink-api", is("http://localhost/$jobName/api"));
  }

  @Test
  public void testConfigWithEmptyPatterns() {
    Mockito.when(appConfig.patterns()).thenReturn(Map.of());
    Mockito.when(appConfig.endpointPathPatterns()).thenReturn(Map.of());

    given()
        .when()
        .get("/config")
        .then()
        .statusCode(200)
        .body("appVersion", notNullValue())
        .body("patterns.size()", is(0))
        .body("endpointPathPatterns.size()", is(0));
  }

  @Test
  public void testConfigWithComplexPatterns() {
    Mockito.when(appConfig.patterns())
        .thenReturn(
            Map.of(
                "display-name", "$metadata.team/$jobName", "description", "$metadata.description"));
    Mockito.when(appConfig.endpointPathPatterns())
        .thenReturn(
            Map.of(
                "flink-ui", "https://flink.$namespace.svc.cluster.local/$jobName/ui",
                "metrics", "https://grafana.example.com/d/$metadata.dashboard_id",
                "logs", "https://kibana.example.com/app/logs?job=$jobName"));

    given()
        .when()
        .get("/config")
        .then()
        .statusCode(200)
        .body("patterns.size()", is(2))
        .body("endpointPathPatterns.size()", is(3))
        .body("patterns.display-name", is("$metadata.team/$jobName"))
        .body(
            "endpointPathPatterns.metrics",
            is("https://grafana.example.com/d/$metadata.dashboard_id"));
  }

  @Test
  public void testConfigEndpointContentType() {
    Mockito.when(appConfig.patterns()).thenReturn(Map.of());
    Mockito.when(appConfig.endpointPathPatterns()).thenReturn(Map.of());

    given().when().get("/config").then().statusCode(200).contentType("application/json");
  }

  @Test
  public void testConfigWithCustomEndpoints() {
    Mockito.when(appConfig.patterns()).thenReturn(Map.of("display-name", "$jobName"));
    Mockito.when(appConfig.endpointPathPatterns())
        .thenReturn(
            Map.of(
                "flink-ui", "http://localhost/$jobName/ui",
                "github-repo", "https://github.com/org/$jobName",
                "grafana", "https://grafana.example.com/d/dashboard?var-job=$jobName",
                "datadog", "https://app.datadoghq.com/logs?query=service:$jobName"));

    given()
        .when()
        .get("/config")
        .then()
        .statusCode(200)
        .body("endpointPathPatterns.size()", is(4))
        .body("endpointPathPatterns.flink-ui", is("http://localhost/$jobName/ui"))
        .body("endpointPathPatterns.github-repo", is("https://github.com/org/$jobName"))
        .body(
            "endpointPathPatterns.grafana",
            is("https://grafana.example.com/d/dashboard?var-job=$jobName"))
        .body(
            "endpointPathPatterns.datadog",
            is("https://app.datadoghq.com/logs?query=service:$jobName"));
  }
}
