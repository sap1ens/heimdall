package com.sap1ens.heimdall.api;

import com.sap1ens.heimdall.model.FlinkJob;
import com.sap1ens.heimdall.service.FlinkJobLocator;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;

@Path("/jobs")
public class FlinkJobResource {

  @Inject Instance<FlinkJobLocator> flinkJobLocator;

  @GET
  public List<FlinkJob> list() {
    return flinkJobLocator.get().findAll();
  }
}
