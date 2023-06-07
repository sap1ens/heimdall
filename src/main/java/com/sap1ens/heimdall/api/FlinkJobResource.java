package com.sap1ens.heimdall.api;

import com.sap1ens.heimdall.model.FlinkJob;
import com.sap1ens.heimdall.service.K8sOperatorFlinkJobLocator;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;

@Path("/jobs")
public class FlinkJobResource {

  @Inject K8sOperatorFlinkJobLocator flinkJobLocator; // TODO: use FlinkJobLocator and parametrize

  @GET
  public List<FlinkJob> list() {
    return flinkJobLocator.findAll();
  }
}
