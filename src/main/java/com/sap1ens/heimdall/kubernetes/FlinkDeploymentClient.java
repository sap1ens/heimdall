package com.sap1ens.heimdall.kubernetes;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;

@Singleton
public class FlinkDeploymentClient {
  private final KubernetesClient ks8Client = new KubernetesClientBuilder().build();

  private final MixedOperation<
          FlinkDeployment, KubernetesResourceList<FlinkDeployment>, Resource<FlinkDeployment>>
      flinkDeploymentK8Client = ks8Client.resources(FlinkDeployment.class);

  public List<FlinkDeployment> find(String namespace, ListOptions listOptions) {
    try {
      Log.debugf("Fetching Flink deployments from namespace: %s", namespace);
      var deployments = flinkDeploymentK8Client.inNamespace(namespace).list(listOptions).getItems();
      Log.debugf("Found %d Flink deployment(s) in namespace: %s", deployments.size(), namespace);
      return deployments;
    } catch (Exception e) {
      Log.errorf(
          e, "Error fetching Flink deployments from namespace '%s': %s", namespace, e.getMessage());
      return new ArrayList<>();
    }
  }

  public List<FlinkDeployment> find(String namespace) {
    return find(namespace, new ListOptions());
  }

  public List<FlinkDeployment> find(List<String> namespaces, ListOptions listOptions) {
    Log.debugf("Fetching Flink deployments from %d namespace(s)", namespaces.size());
    List<FlinkDeployment> allDeployments = new ArrayList<>();
    for (String namespace : namespaces) {
      allDeployments.addAll(find(namespace, listOptions));
    }
    Log.infof(
        "Fetched %d total Flink deployment(s) from namespaces: %s",
        allDeployments.size(), namespaces);
    return allDeployments;
  }

  public List<FlinkDeployment> find(List<String> namespaces) {
    return find(namespaces, new ListOptions());
  }
}
