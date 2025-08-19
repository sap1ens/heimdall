package com.sap1ens.heimdall.service;

import com.sap1ens.heimdall.AppConfig;
import com.sap1ens.heimdall.kubernetes.FlinkDeploymentClient;
import com.sap1ens.heimdall.model.FlinkJob;
import com.sap1ens.heimdall.model.FlinkJobResources;
import com.sap1ens.heimdall.model.FlinkJobType;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.apache.flink.kubernetes.operator.api.spec.JobSpec;

@Singleton
@LookupIfProperty(name = "heimdall.joblocator.k8s-operator.enabled", stringValue = "true")
public class K8sOperatorFlinkJobLocator implements FlinkJobLocator {
  private static final String TM_NUMBER_OF_TASK_SLOTS = "taskmanager.numberOfTaskSlots";
  private static final String UNKNOWN_STATUS = "UNKNOWN";
  private static final String JM_LABEL = "jm";
  private static final String TM_LABEL = "tm";

  @Inject AppConfig appConfig;

  @Inject FlinkDeploymentClient flinkDeploymentClient;

  @Override
  public List<FlinkJob> findAll() {
    var namespaces = appConfig.joblocator().k8sOperator().namespacesToWatch();
    var flinkDeployments = flinkDeploymentClient.find(namespaces);
    return flinkDeployments.stream().map(this::toFlinkJob).collect(Collectors.toList());
  }

  private FlinkJob toFlinkJob(FlinkDeployment flinkDeployment) {
    var jobType = getJobType(flinkDeployment);

    var jmSpec = flinkDeployment.getSpec().getJobManager();
    var tmSpec = flinkDeployment.getSpec().getTaskManager();

    var tmReplicas = Optional.ofNullable(tmSpec.getReplicas()).orElse(0);
    if (tmReplicas == 0 && jobType.equals(FlinkJobType.APPLICATION)) {
      // Try getting the number of replicas from the status
      tmReplicas = flinkDeployment.getStatus().getTaskManager().getReplicas();
    }

    return new FlinkJob(
        flinkDeployment.getMetadata().getUid(),
        flinkDeployment.getMetadata().getName(),
        flinkDeployment.getMetadata().getNamespace(),
        Optional.ofNullable(flinkDeployment.getStatus().getJobStatus().getState())
            .map(Enum::toString)
            .orElse(UNKNOWN_STATUS),
        jobType,
        Optional.ofNullable(flinkDeployment.getStatus().getJobStatus().getStartTime())
            .map(Long::parseLong)
            .orElse(null),
        getShortImage(flinkDeployment),
        getFlinkVersion(flinkDeployment),
        getParallelism(flinkDeployment),
        Map.of(
            JM_LABEL,
                new FlinkJobResources(
                    jmSpec.getReplicas(),
                    jmSpec.getResource().getCpu().toString(),
                    jmSpec.getResource().getMemory()),
            TM_LABEL,
                new FlinkJobResources(
                    tmReplicas,
                    tmSpec.getResource().getCpu().toString(),
                    tmSpec.getResource().getMemory())),
        flinkDeployment.getMetadata().getLabels());
  }

  protected FlinkJobType getJobType(FlinkDeployment flinkDeployment) {
    return flinkDeployment.getSpec().getJob() == null
        ? FlinkJobType.SESSION
        : FlinkJobType.APPLICATION;
  }

  protected String getShortImage(FlinkDeployment flinkDeployment) {
    var image = flinkDeployment.getSpec().getImage();
    return image.contains("/") ? flinkDeployment.getSpec().getImage().split("/")[1] : image;
  }

  protected int getParallelism(FlinkDeployment flinkDeployment) {
    var parallelism = 0;
    // First, try to get parallelism from the job spec
    var jobSpecParallelism =
        Optional.ofNullable(flinkDeployment.getSpec().getJob())
            .map(JobSpec::getParallelism)
            .orElse(0);
    if (jobSpecParallelism != 0) {
      parallelism = jobSpecParallelism;
      // If parallelism is not set in the job spec, try to calculate it based on the number of
      // replicas and configured task slots
      // FIXME: this might not be accurate
    } else {
      var taskSlots =
          Optional.ofNullable(flinkDeployment.getSpec().getFlinkConfiguration())
              .map(config -> config.get(TM_NUMBER_OF_TASK_SLOTS))
              .orElse(null);
      var replicas = flinkDeployment.getSpec().getTaskManager().getReplicas();
      if (taskSlots != null && replicas != null) {
        parallelism = Integer.parseInt(taskSlots) * replicas;
      }
    }
    return parallelism;
  }

  protected String getFlinkVersion(FlinkDeployment flinkDeployment) {
    String rawVersion = flinkDeployment.getSpec().getFlinkVersion().toString();
    return rawVersion.replace("_", ".").replace("v", "");
  }
}
