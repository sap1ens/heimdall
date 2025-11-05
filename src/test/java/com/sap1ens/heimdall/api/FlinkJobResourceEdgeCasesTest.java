package com.sap1ens.heimdall.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import com.sap1ens.heimdall.model.FlinkJob;
import com.sap1ens.heimdall.model.FlinkJobResources;
import com.sap1ens.heimdall.model.FlinkJobType;
import com.sap1ens.heimdall.service.FlinkJobLocator;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.MockitoConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(FlinkJobResourceEdgeCasesTest.NoCacheTestProfile.class)
public class FlinkJobResourceEdgeCasesTest {

  public static class NoCacheTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("quarkus.cache.enabled", "false");
    }
  }

  @InjectMock
  @MockitoConfig(convertScopes = true)
  FlinkJobLocator flinkJobLocator;

  @Test
  public void testJobWithNullFields() {
    Mockito.when(flinkJobLocator.findAll())
        .thenReturn(
            List.of(
                new FlinkJob(
                    "id1",
                    "job1",
                    "default",
                    "RUNNING",
                    FlinkJobType.APPLICATION,
                    null, // null startTime
                    null, // null image
                    null, // null flinkVersion
                    null, // null parallelism
                    Collections.emptyMap(),
                    Collections.emptyMap())));

    given()
        .when()
        .get("/jobs")
        .then()
        .statusCode(200)
        .body("size()", is(1))
        .body("[0].id", is("id1"))
        .body("[0].startTime", nullValue())
        .body("[0].shortImage", nullValue())
        .body("[0].flinkVersion", nullValue())
        .body("[0].parallelism", nullValue());
  }

  @Test
  public void testMultipleJobsWithDifferentTypes() {
    Mockito.when(flinkJobLocator.findAll())
        .thenReturn(
            List.of(
                new FlinkJob(
                    "app-job",
                    "application-job",
                    "default",
                    "RUNNING",
                    FlinkJobType.APPLICATION,
                    1687261027814L,
                    "app-image:1.0",
                    "1.15",
                    4,
                    Collections.emptyMap(),
                    Map.of("env", "prod")),
                new FlinkJob(
                    "session-job",
                    "session-job",
                    "staging",
                    "FAILED",
                    FlinkJobType.SESSION,
                    1687261027815L,
                    "session-image:2.0",
                    "1.16",
                    8,
                    Collections.emptyMap(),
                    Map.of("env", "staging"))));

    given()
        .when()
        .get("/jobs")
        .then()
        .statusCode(200)
        .body("size()", is(2))
        .body("[0].type", is("APPLICATION"))
        .body("[1].type", is("SESSION"))
        .body("[0].status", is("RUNNING"))
        .body("[1].status", is("FAILED"));
  }

  @Test
  public void testJobWithComplexResources() {
    var tmResources = new FlinkJobResources(4, "2.0", "4096m");
    var jmResources = new FlinkJobResources(1, "1.0", "2048m");
    var resources = Map.of("tm", tmResources, "jm", jmResources);

    Mockito.when(flinkJobLocator.findAll())
        .thenReturn(
            List.of(
                new FlinkJob(
                    "resource-job",
                    "resource-test",
                    "default",
                    "RUNNING",
                    FlinkJobType.APPLICATION,
                    1687261027814L,
                    "test:1.0",
                    "1.15",
                    4,
                    resources,
                    Collections.emptyMap())));

    given()
        .when()
        .get("/jobs")
        .then()
        .statusCode(200)
        .body("size()", is(1))
        .body("[0].resources.tm.replicas", is(4))
        .body("[0].resources.tm.cpu", is("2.0"))
        .body("[0].resources.tm.mem", is("4096m"))
        .body("[0].resources.jm.replicas", is(1))
        .body("[0].resources.jm.cpu", is("1.0"))
        .body("[0].resources.jm.mem", is("2048m"));
  }

  @Test
  public void testJobWithMetadata() {
    var metadata =
        Map.of(
            "team", "platform",
            "environment", "production",
            "version", "v1.2.3",
            "owner", "john.doe");

    Mockito.when(flinkJobLocator.findAll())
        .thenReturn(
            List.of(
                new FlinkJob(
                    "metadata-job",
                    "metadata-test",
                    "default",
                    "RUNNING",
                    FlinkJobType.APPLICATION,
                    1687261027814L,
                    "test:1.0",
                    "1.15",
                    4,
                    Collections.emptyMap(),
                    metadata)));

    given()
        .when()
        .get("/jobs")
        .then()
        .statusCode(200)
        .body("size()", is(1))
        .body("[0].metadata.team", is("platform"))
        .body("[0].metadata.environment", is("production"))
        .body("[0].metadata.version", is("v1.2.3"))
        .body("[0].metadata.owner", is("john.doe"));
  }

  @Test
  public void testLargeNumberOfJobs() {
    var jobs =
        List.of(
            createJob("job1", "ns1"),
            createJob("job2", "ns1"),
            createJob("job3", "ns2"),
            createJob("job4", "ns2"),
            createJob("job5", "ns3"),
            createJob("job6", "ns3"),
            createJob("job7", "ns4"),
            createJob("job8", "ns4"),
            createJob("job9", "ns5"),
            createJob("job10", "ns5"));

    Mockito.when(flinkJobLocator.findAll()).thenReturn(jobs);

    given().when().get("/jobs").then().statusCode(200).body("size()", is(10));
  }

  @Test
  public void testJobsWithSpecialCharactersInName() {
    Mockito.when(flinkJobLocator.findAll())
        .thenReturn(
            List.of(
                new FlinkJob(
                    "special-job",
                    "job-with-dashes_and_underscores.dots",
                    "default",
                    "RUNNING",
                    FlinkJobType.APPLICATION,
                    1687261027814L,
                    "test:1.0",
                    "1.15",
                    4,
                    Collections.emptyMap(),
                    Collections.emptyMap())));

    given()
        .when()
        .get("/jobs")
        .then()
        .statusCode(200)
        .body("size()", is(1))
        .body("[0].name", is("job-with-dashes_and_underscores.dots"));
  }

  @Test
  public void testJobsWithAllStatuses() {
    Mockito.when(flinkJobLocator.findAll())
        .thenReturn(
            List.of(
                createJobWithStatus("job1", "RUNNING"),
                createJobWithStatus("job2", "FAILED"),
                createJobWithStatus("job3", "FINISHED"),
                createJobWithStatus("job4", "SUSPENDED"),
                createJobWithStatus("job5", "CANCELLING")));

    given()
        .when()
        .get("/jobs")
        .then()
        .statusCode(200)
        .body("size()", is(5))
        .body("[0].status", is("RUNNING"))
        .body("[1].status", is("FAILED"))
        .body("[2].status", is("FINISHED"))
        .body("[3].status", is("SUSPENDED"))
        .body("[4].status", is("CANCELLING"));
  }

  private FlinkJob createJob(String name, String namespace) {
    return new FlinkJob(
        name + "-id",
        name,
        namespace,
        "RUNNING",
        FlinkJobType.APPLICATION,
        System.currentTimeMillis(),
        "image:1.0",
        "1.15",
        4,
        Collections.emptyMap(),
        Collections.emptyMap());
  }

  private FlinkJob createJobWithStatus(String name, String status) {
    return new FlinkJob(
        name + "-id",
        name,
        "default",
        status,
        FlinkJobType.APPLICATION,
        System.currentTimeMillis(),
        "image:1.0",
        "1.15",
        4,
        Collections.emptyMap(),
        Collections.emptyMap());
  }
}
