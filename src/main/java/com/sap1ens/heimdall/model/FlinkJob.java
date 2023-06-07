package com.sap1ens.heimdall.model;

import java.util.Optional;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;

public record FlinkJob(
    String name, String status, Long startTime, String image, Integer parallelism) {
  public static FlinkJob from(FlinkDeployment flinkDeployment) {
    var containerTemplate =
        Optional.ofNullable(flinkDeployment.getSpec().getPodTemplate())
            .map(podTemplate -> podTemplate.getSpec().getContainers().get(0));
    var image = flinkDeployment.getSpec().getImage();

    return new FlinkJob(
        flinkDeployment.getMetadata().getName(),
        flinkDeployment.getStatus().getJobStatus().getState(),
        Optional.ofNullable(flinkDeployment.getStatus().getJobStatus().getStartTime())
            .map(Long::parseLong)
            .orElse(null),
        image, // TODO
        0 // TODO
        );
  }
}
