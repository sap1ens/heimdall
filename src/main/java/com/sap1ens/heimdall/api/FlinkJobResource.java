package com.sap1ens.heimdall.api;

import com.sap1ens.heimdall.model.FlinkJob;
import com.sap1ens.heimdall.service.FlinkJobLocator;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;

@Blocking
@Path("/jobs")
public class FlinkJobResource {

  @Inject Instance<FlinkJobLocator> flinkJobLocator;

  @CacheResult(cacheName = "flink-jobs")
  @GET
  public List<FlinkJob> list() {
    Log.debug("Received request to list Flink jobs");
    var jobs = flinkJobLocator.get().findAll();
    Log.infof("Returning %d Flink job(s)", jobs.size());
    return jobs;
  }
}
