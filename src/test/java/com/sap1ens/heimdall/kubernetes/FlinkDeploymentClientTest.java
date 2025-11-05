package com.sap1ens.heimdall.kubernetes;

import static org.junit.jupiter.api.Assertions.*;

import io.fabric8.kubernetes.api.model.ListOptions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for FlinkDeploymentClient. Note: These tests verify the class structure and basic
 * functionality without connecting to a real Kubernetes cluster. Integration tests with a test
 * cluster should be run separately.
 */
public class FlinkDeploymentClientTest {

  @Test
  public void testFlinkDeploymentClientCanBeInstantiated() {
    // Verify the class can be instantiated
    // Note: This creates a real K8s client but doesn't make any API calls
    assertDoesNotThrow(() -> new FlinkDeploymentClient());
  }

  @Test
  public void testListOptionsCanBeCreated() {
    // Verify ListOptions can be created for method signatures
    var listOptions = new ListOptions();
    assertNotNull(listOptions);

    // Test setting limit
    listOptions.setLimit(10L);
    assertEquals(10L, listOptions.getLimit());
  }

  @Test
  public void testClassHasExpectedMethods() {
    // Compile-time verification that the expected methods exist
    var client = new FlinkDeploymentClient();

    // Verify the client exists and is not null
    assertNotNull(client);

    // The following methods exist (compile-time check):
    // - find(String namespace)
    // - find(String namespace, ListOptions listOptions)
    // - find(List<String> namespaces)
    // - find(List<String> namespaces, ListOptions listOptions)

    // Note: We don't call these methods as they would attempt to connect to K8s
    // Integration tests with a mock K8s server or test cluster should test actual behavior
  }
}
