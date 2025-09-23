package com.sap1ens.heimdall.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import com.sap1ens.heimdall.model.FlinkJob;
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
@TestProfile(FlinkJobResourceTest.NoCacheTestProfile.class)
public class FlinkJobResourceTest {

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
  public void testJobListingEmpty() {
    Mockito.when(flinkJobLocator.findAll()).thenReturn(Collections.emptyList());

    given().when().get("/jobs").then().statusCode(200).body(is("[]"));
  }

  @Test
  public void testJobListing() {
    Mockito.when(flinkJobLocator.findAll())
        .thenReturn(
            List.of(
                new FlinkJob(
                    "testId",
                    "testName",
                    "default",
                    "RUNNING",
                    FlinkJobType.APPLICATION,
                    1687261027814L,
                    "testImage",
                    null,
                    1,
                    Collections.emptyMap(),
                    Collections.emptyMap())));

    var payload =
        """
        [{"id":"testId","name":"testName","namespace":"default","status":"RUNNING","type":"APPLICATION","startTime":1687261027814,"shortImage":"testImage","flinkVersion":null,"parallelism":1,"resources":{},"metadata":{}}]""";

    given().when().get("/jobs").then().statusCode(200).body(is(payload));
  }
}
