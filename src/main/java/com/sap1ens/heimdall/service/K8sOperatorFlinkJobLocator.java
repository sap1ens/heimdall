package com.sap1ens.heimdall.service;

import com.sap1ens.heimdall.AppConfig;
import com.sap1ens.heimdall.kubernetes.FlinkDeploymentClient;
import com.sap1ens.heimdall.model.FlinkJob;
import com.sap1ens.heimdall.model.FlinkJobResources;
import com.sap1ens.heimdall.model.FlinkJobType;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.logging.Log;
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
    Log.debugf("Searching for Flink deployments in namespaces: %s", namespaces);

    var flinkDeployments = flinkDeploymentClient.find(namespaces);
    Log.infof("Found %d Flink deployment(s) in namespaces: %s", flinkDeployments.size(), namespaces);

    return flinkDeployments.stream().map(this::toFlinkJob).collect(Collectors.toList());
  }

  private FlinkJob toFlinkJob(FlinkDeployment flinkDeployment) {
    var deploymentName = flinkDeployment.getMetadata().getName();
    var namespace = flinkDeployment.getMetadata().getNamespace();
    Log.debugf("Processing Flink deployment '%s' in namespace '%s'", deploymentName, namespace);

    var jobType = getJobType(flinkDeployment);

    var jmSpec = flinkDeployment.getSpec().getJobManager();
    var tmSpec = flinkDeployment.getSpec().getTaskManager();

    var tmReplicas = Optional.ofNullable(tmSpec.getReplicas()).orElse(0);
    if (tmReplicas == 0 && jobType.equals(FlinkJobType.APPLICATION)) {
      // Try getting the number of replicas from the status
      tmReplicas = flinkDeployment.getStatus().getTaskManager().getReplicas();
      Log.debugf(
          "Task manager replicas not set in spec for '%s', using status value: %d",
          deploymentName, tmReplicas);
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
    return image.contains("/") ? image.substring(image.indexOf("/") + 1) : image;
  }

  protected int getParallelism(FlinkDeployment flinkDeployment) {
    var parallelism = 0;
    var deploymentName = flinkDeployment.getMetadata().getName();

    // First, try to get parallelism from the job spec
    var jobSpecParallelism =
        Optional.ofNullable(flinkDeployment.getSpec().getJob())
            .map(JobSpec::getParallelism)
            .orElse(0);

    if (jobSpecParallelism != 0) {
      parallelism = jobSpecParallelism;
      Log.debugf(
          "Using job spec parallelism %d for deployment '%s'", parallelism, deploymentName);
    } else {
      // If parallelism is not set in the job spec, try to calculate it based on the number of
      // task manager replicas and configured task slots.
      // NOTE: This calculation assumes all task slots are available and may not be accurate in
      // scenarios where:
      // - Task slots are partially occupied by other jobs (in session clusters)
      // - Auto-scaling is enabled and replicas change dynamically
      // - Custom slot sharing groups are configured
      var taskSlots =
          Optional.ofNullable(flinkDeployment.getSpec().getFlinkConfiguration())
              .map(config -> config.get(TM_NUMBER_OF_TASK_SLOTS))
              .orElse(null);
      var replicas = flinkDeployment.getSpec().getTaskManager().getReplicas();

      if (taskSlots != null && replicas != null) {
        parallelism = Integer.parseInt(taskSlots) * replicas;
        Log.debugf(
            "Calculated parallelism %d (taskSlots: %s, replicas: %d) for deployment '%s'",
            parallelism, taskSlots, replicas, deploymentName);
      } else {
        Log.warnf(
            "Could not determine parallelism for deployment '%s': taskSlots=%s, replicas=%s",
            deploymentName, taskSlots, replicas);
      }
    }
    return parallelism;
  }

  protected String getFlinkVersion(FlinkDeployment flinkDeployment) {
    String rawVersion = flinkDeployment.getSpec().getFlinkVersion().toString();
    return rawVersion.replace("_", ".").replace("v", "");
  }
}
