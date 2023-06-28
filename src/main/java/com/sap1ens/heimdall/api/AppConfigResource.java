package com.sap1ens.heimdall.api;

import com.sap1ens.heimdall.AppConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/config")
public class AppConfigResource {

  @Inject AppConfig appConfig;

  @ConfigProperty(name = "quarkus.application.version")
  String appVersion;

  @GET
  public Map<String, Object> index() {
    // Only cherry-picking certain properties, don't need to show the whole config
    return Map.of(
        "appVersion", appVersion,
        "patterns", appConfig.patterns(),
        "endpointPathPatterns", appConfig.endpointPathPatterns());
  }
}
