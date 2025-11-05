package com.sap1ens.heimdall.kubernetes;

import static org.junit.jupiter.api.Assertions.*;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class FlinkDeploymentClientTest {

  @Inject FlinkDeploymentClient flinkDeploymentClient;

  @Test
  public void testFindInSingleNamespace() {
    var deployments = flinkDeploymentClient.find("default");
    assertNotNull(deployments);
    // In real environment, this would query K8s cluster
    // For unit tests, we're verifying the method executes without errors
  }

  @Test
  public void testFindInSingleNamespaceWithOptions() {
    var listOptions = new ListOptions();
    listOptions.setLimit(10L);
    var deployments = flinkDeploymentClient.find("default", listOptions);
    assertNotNull(deployments);
  }

  @Test
  public void testFindInMultipleNamespaces() {
    var namespaces = List.of("default", "prod", "staging");
    var deployments = flinkDeploymentClient.find(namespaces);
    assertNotNull(deployments);
  }

  @Test
  public void testFindInMultipleNamespacesWithOptions() {
    var listOptions = new ListOptions();
    listOptions.setLimit(10L);
    var namespaces = List.of("default", "prod");
    var deployments = flinkDeploymentClient.find(namespaces, listOptions);
    assertNotNull(deployments);
  }

  @Test
  public void testFindWithEmptyNamespaceList() {
    var deployments = flinkDeploymentClient.find(List.of());
    assertNotNull(deployments);
    assertTrue(deployments.isEmpty());
  }

  @Test
  public void testFindWithNullOptions() {
    // Verify that null options are handled (should default to ListOptions())
    assertDoesNotThrow(() -> flinkDeploymentClient.find("default", null));
  }
}
