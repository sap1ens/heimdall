package com.sap1ens.heimdall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.config.SmallRyeConfig;
import java.util.Map;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(AppConfigTest.EmptyEndpointPathPatternTestProfile.class)
public class AppConfigTest {

  public static class EmptyEndpointPathPatternTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      // Per the README, an empty string disables the endpoint link.
      return Map.of("heimdall.endpoint-path-patterns.logs", "");
    }
  }

  @Test
  public void testEmptyEndpointPathPatternIsExcluded() {
    var config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
    var appConfig = config.getConfigMapping(AppConfig.class);

    var patterns = appConfig.endpointPathPatterns();
    assertEquals("http://localhost/$jobName/ui", patterns.get("flink-ui"));
    assertFalse(patterns.containsKey("logs"));
  }
}
