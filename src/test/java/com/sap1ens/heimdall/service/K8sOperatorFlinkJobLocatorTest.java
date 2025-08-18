package com.sap1ens.heimdall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sap1ens.heimdall.AppConfig;
import com.sap1ens.heimdall.kubernetes.FlinkDeploymentClient;
import com.sap1ens.heimdall.model.FlinkJobType;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.apache.flink.kubernetes.operator.api.spec.FlinkDeploymentSpec;
import org.apache.flink.kubernetes.operator.api.spec.FlinkVersion;
import org.apache.flink.kubernetes.operator.api.spec.JobManagerSpec;
import org.apache.flink.kubernetes.operator.api.spec.JobSpec;
import org.apache.flink.kubernetes.operator.api.spec.Resource;
import org.apache.flink.kubernetes.operator.api.spec.TaskManagerSpec;
import org.apache.flink.kubernetes.operator.api.status.FlinkDeploymentStatus;
import org.apache.flink.kubernetes.operator.api.status.JobStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class K8sOperatorFlinkJobLocatorTest {

  @InjectMock
  @MockitoConfig(convertScopes = true)
  FlinkDeploymentClient flinkDeploymentClient;

  @InjectMock
  @MockitoConfig(returnsDeepMocks = true)
  AppConfig appConfig;

  @Inject K8sOperatorFlinkJobLocator flinkJobLocator;

  @Test
  public void testFindAllEmpty() {
    Mockito.when(appConfig.joblocator().k8sOperator().namespaceToWatch()).thenReturn("empty-one");
    Mockito.when(appConfig.joblocator().k8sOperator().namespacesToWatch())
        .thenReturn(List.of("empty-one"));
    Mockito.when(flinkDeploymentClient.find(List.of("empty-one")))
        .thenReturn(Collections.emptyList());

    assertTrue(flinkJobLocator.findAll().isEmpty());
  }

  @Test
  public void testFindAll() {
    var flinkDeployments = generateFlinkDeployments(2);
    Mockito.when(appConfig.joblocator().k8sOperator().namespaceToWatch()).thenReturn("default");
    Mockito.when(appConfig.joblocator().k8sOperator().namespacesToWatch())
        .thenReturn(List.of("default"));
    Mockito.when(flinkDeploymentClient.find(List.of("default"))).thenReturn(flinkDeployments);

    var flinkJobs = flinkJobLocator.findAll();
    assertEquals(2, flinkJobs.size());
    var flinkJob = flinkJobs.get(0);
    assertEquals("test-flink-deployment", flinkJob.name());
    assertEquals("RUNNING", flinkJob.status());
    assertEquals(FlinkJobType.APPLICATION, flinkJob.type());
    assertEquals("test-image", flinkJob.shortImage());
    assertEquals("1.15", flinkJob.flinkVersion());
    assertEquals(4, flinkJob.parallelism());
    assertEquals(4, flinkJob.resources().get("tm").replicas());
    assertEquals("1.0", flinkJob.resources().get("tm").cpu());
    assertEquals("2048m", flinkJob.resources().get("tm").mem());
    assertEquals("test-flink-deployment", flinkJob.metadata().get("flink-app"));
  }

  @Test
  public void testFindAllMultipleNamespaces() {
    var flinkDeployments = generateFlinkDeployments(3);
    Mockito.when(appConfig.joblocator().k8sOperator().namespaceToWatch())
        .thenReturn("default,prod,staging");
    Mockito.when(appConfig.joblocator().k8sOperator().namespacesToWatch())
        .thenReturn(List.of("default", "prod", "staging"));
    Mockito.when(flinkDeploymentClient.find(List.of("default", "prod", "staging")))
        .thenReturn(flinkDeployments);

    var flinkJobs = flinkJobLocator.findAll();
    assertEquals(3, flinkJobs.size());
  }

  private List<FlinkDeployment> generateFlinkDeployments(int num) {
    var flinkDeployments = new ArrayList<FlinkDeployment>();
    for (int i = 0; i < num; i++) {
      flinkDeployments.add(generateFlinkDeployment());
    }
    return flinkDeployments;
  }

  private FlinkDeployment generateFlinkDeployment() {
    var flinkDeployment = new FlinkDeployment();
    flinkDeployment.setMetadata(
        new ObjectMetaBuilder()
            .withUid(UUID.randomUUID().toString())
            .withName("test-flink-deployment")
            .withNamespace("default")
            .withLabels(Map.of("flink-app", "test-flink-deployment"))
            .build());
    var jobSpec = new JobSpec();
    jobSpec.setParallelism(4);
    var spec = new FlinkDeploymentSpec();
    spec.setImage("test-image");
    spec.setFlinkVersion(FlinkVersion.v1_15);
    spec.setJob(jobSpec);
    var resource = new Resource();
    resource.setMemory("2048m");
    resource.setCpu(1.0);
    var jm = new JobManagerSpec();
    jm.setResource(resource);
    jm.setReplicas(1);
    spec.setJobManager(jm);
    var tm = new TaskManagerSpec();
    tm.setResource(resource);
    tm.setReplicas(4);
    spec.setTaskManager(tm);
    flinkDeployment.setSpec(spec);
    var status = new FlinkDeploymentStatus();
    var jobStatus = new JobStatus();
    jobStatus.setState(org.apache.flink.api.common.JobStatus.RUNNING);
    status.setJobStatus(jobStatus);
    flinkDeployment.setStatus(status);
    return flinkDeployment;
  }

  @ApplicationScoped
  @Mock
  public static class MockedAppConfig implements AppConfig {
    @Override
    public Joblocator joblocator() {
      return null;
    }

    @Override
    public Map<String, String> patterns() {
      return null;
    }

    @Override
    public Map<String, String> endpointPathPatterns() {
      return null;
    }
  }
}
