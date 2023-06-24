package com.sap1ens.heimdall.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import com.sap1ens.heimdall.model.FlinkJob;
import com.sap1ens.heimdall.model.FlinkJobType;
import com.sap1ens.heimdall.service.FlinkJobLocator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class FlinkJobResourceTest {

  @InjectMock(convertScopes = true)
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
                    "RUNNING",
                    FlinkJobType.APPLICATION,
                    1687261027814L,
                    "testImage",
                    1,
                    Collections.emptyMap())));

    var payload =
        """
        [{"id":"testId","name":"testName","status":"RUNNING","type":"APPLICATION","startTime":1687261027814,"shortImage":"testImage","parallelism":1,"resources":{}}]""";

    given().when().get("/jobs").then().statusCode(200).body(is(payload));
  }
}
