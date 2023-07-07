package com.sap1ens.heimdall.kubernetes;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import jakarta.inject.Singleton;
import java.util.List;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;

@Singleton
public class FlinkDeploymentClient {
  private final KubernetesClient ks8Client = new KubernetesClientBuilder().build();

  private final MixedOperation<
          FlinkDeployment, KubernetesResourceList<FlinkDeployment>, Resource<FlinkDeployment>>
      flinkDeploymentK8Client = ks8Client.resources(FlinkDeployment.class);

  public List<FlinkDeployment> find(String namespace, ListOptions listOptions) {
    return flinkDeploymentK8Client.inNamespace(namespace).list(listOptions).getItems();
  }

  public List<FlinkDeployment> find(String namespace) {
    return find(namespace, new ListOptions());
  }
}
