package com.sap1ens.heimdall.service;

import com.sap1ens.heimdall.model.FlinkJob;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class K8sOperatorFlinkJobLocator implements FlinkJobLocator {
  @ConfigProperty(name = "heimdall.k8s.namespace-to-watch")
  String k8sNamespace;

  private KubernetesClient ks8Client = new KubernetesClientBuilder().build();

  private MixedOperation<
          FlinkDeployment, KubernetesResourceList<FlinkDeployment>, Resource<FlinkDeployment>>
      flinkDeploymentK8Client = ks8Client.resources(FlinkDeployment.class);

  @Override
  public List<FlinkJob> findAll() {
    var listOptions = new ListOptions();
    var flinkDeployments =
        flinkDeploymentK8Client.inNamespace(k8sNamespace).list(listOptions).getItems();
    return flinkDeployments.stream().map(FlinkJob::from).collect(Collectors.toList());
  }
}
